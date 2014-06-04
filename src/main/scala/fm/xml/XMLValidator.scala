/*
 * Copyright 2014 Frugal Mechanic (http://frugalmechanic.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fm.xml

import fm.common.{InputStreamResource, Resource, SingleUseResource}
import com.ctc.wstx.stax.WstxInputFactory
import com.ctc.wstx.exc.WstxParsingException
import com.frugalmechanic.optparse._
import java.io.{File, FileInputStream, Reader}
import javax.xml.stream.XMLInputFactory
import org.codehaus.stax2.validation.XMLValidationSchemaFactory
import org.codehaus.stax2.validation.XMLValidationSchema
import org.codehaus.stax2.XMLStreamReader2
import org.codehaus.stax2.XMLInputFactory2
import org.codehaus.stax2.validation.Validatable
import org.codehaus.stax2.validation.ValidationProblemHandler
import org.codehaus.stax2.validation.XMLValidationException
import org.codehaus.stax2.validation.XMLValidationProblem

object XMLValidator {
  object Options extends OptParse {
    val xsd = FileOpt()
    val xml = FileOpt()
  }
  
  def main(args: Array[String]): Unit = {
    Options.parse(args)
    val result: ValidationResult = validate(Options.xsd(), Options.xml())
    println(result)
  }
  
  final class ValidationResultBuilder(limit: Option[Int]) extends ValidationProblemHandler {
    def this() = this(None)
    def this(limit: Int) = this(Some(limit))
    
    private[this] var validationErrorCount: Int = 0
    private[this] val validationErrors = Vector.newBuilder[ErrorMessage]
    
    def reportProblem(problem: XMLValidationProblem): Unit = {          
      if (limit.isEmpty || validationErrorCount < limit.get) validationErrors += ErrorMessage(problem.getMessage, problem.getLocation)
      validationErrorCount += 1
    }
    
    def result(): ValidationResult = {
      if (validationErrorCount > 0) ValidationFailed(validationErrorCount, validationErrors.result) else Success
    }
  }
  
  sealed trait ValidationResult {
    def isValid: Boolean
    def failedValidation: Boolean
    def failedParsing: Boolean
    def errors: Seq[ErrorMessage]
    def totalErrorCount: Int
    final def truncatedErrorCount: Int = totalErrorCount - errors.size
  }
  
  case object Success extends ValidationResult {
    def isValid: Boolean = true
    def failedValidation: Boolean = false
    def failedParsing: Boolean = false
    def errors: Seq[ErrorMessage] = Nil
    def totalErrorCount: Int = 0
  }

  final case class ParseError(error: ErrorMessage) extends ValidationResult {
    def msg: String = error.msg
    def location: Location = error.location
    
    def isValid: Boolean = false
    def failedValidation: Boolean = true
    def failedParsing: Boolean = true
    
    def errors: Seq[ErrorMessage] = Seq(error)
    def totalErrorCount: Int = 1
  }
  
  final case class ValidationFailed(totalErrorCount: Int, errors: Vector[ErrorMessage]) extends ValidationResult {    
    def isValid: Boolean = false
    def failedValidation: Boolean = true
    def failedParsing: Boolean = false
  }
  
  final case class ErrorMessage(msg: String, location: Location) {
    def line: Int = location.line
    def column: Int = location.column
    def charOffset: Int = location.charOffset
  }
  
  final case class Location(line: Int, column: Int, charOffset: Int)
  private implicit def toLocation(loc: javax.xml.stream.Location): Location = Location(loc.getLineNumber, loc.getColumnNumber, loc.getCharacterOffset)
  
  /** Creates a XMLValidationSchema (which is thread-safe and reusable!) for an XSD */
  def createXMLValidationSchema(xsd: File): XMLValidationSchema = InputStreamResource.forFileOrResource(xsd).reader().use{ createXMLValidationSchema }
  
  /** Creates a XMLValidationSchema (which is thread-safe and reusable!) for an XSD */
  def createXMLValidationSchema(xsd: Resource[Reader]): XMLValidationSchema = xsd.use{ createXMLValidationSchema }
  
  /** Creates a XMLValidationSchema (which is thread-safe and reusable!) for an XSD */
  def createXMLValidationSchema(xsd: Reader): XMLValidationSchema = {
    val sf: XMLValidationSchemaFactory = XMLValidationSchemaFactory.newInstance(XMLValidationSchema.SCHEMA_ID_W3C_SCHEMA)
    val vs: XMLValidationSchema = sf.createSchema(xsd)
    vs
  }
  
  //
  // These are for stand-alone validation
  //
  
  def validate(xsd: File, xml: File): ValidationResult = validate(xsd, xml, None)
  def validate(xsd: File, xml: File, limit: Option[Int]): ValidationResult = Resource.use(InputStreamResource.forFileOrResource(xsd).reader(), InputStreamResource.forFileOrResource(xml).reader()){ validate(_, _, limit) }
  def validate(xsd: Resource[Reader], xml: Resource[Reader]): ValidationResult = validate(xsd, xml, None)
  def validate(xsd: Resource[Reader], xml: Resource[Reader], limit: Option[Int]): ValidationResult = Resource.use(xsd, xml){ validate(_, _, limit) }
  def validate(xsd: Reader, xml: Reader, limit: Option[Int]): ValidationResult = validate(createXMLValidationSchema(xsd), xml, limit)
  
  /**
   * Stand-alone validation against an xml Reader.  This method will construct an XML Reader and read the full contents of the Reader
   */
  def validate(xsd: XMLValidationSchema, xml: Reader, limit: Option[Int]): ValidationResult = {
    
    val inputFactory = new WstxInputFactory()
    inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false)
    inputFactory.configureForSpeed()
    
    import Resource.toCloseable // Need this implicit for the next line to work
    
    SingleUseResource(inputFactory.createXMLStreamReader(xml).asInstanceOf[XMLStreamReader2]).use { sr: XMLStreamReader2 =>

      val resultBuilder: ValidationResultBuilder = validate(xsd, sr, limit)
      
      try {
        while (sr.hasNext()) { sr.next() }
        resultBuilder.result()
      } catch {
        case ex: WstxParsingException => ParseError(ErrorMessage(ex.getMessage, ex.getLocation))
        // This means we've hit our limit for validation errors and need to stop
        case ex: XMLValidationException => resultBuilder.result()
      }
    }
  }

  //
  // These are for pluggable validation against an existing Validatable instance
  //
  
  def validate(xsd: File, validatable: Validatable, limit: Option[Int]): ValidationResultBuilder = validate(InputStreamResource.forFileOrResource(xsd).reader(), validatable, limit)
  def validate(xsd: Resource[Reader], validatable: Validatable, limit: Option[Int]): ValidationResultBuilder = xsd.use{ validate(_, validatable, limit) }
  def validate(xsd: Reader, validatable: Validatable, limit: Option[Int]): ValidationResultBuilder = validate(createXMLValidationSchema(xsd), validatable, limit)
  
  /**
   * Plug into an existing Validatable instance.  You get back a ValidationResultBuilder that you can call
   * the result() method on once you are done reading/writing with the Validatable instance.
   */
  def validate(xsd: XMLValidationSchema, validatable: Validatable, limit: Option[Int]): ValidationResultBuilder = {
    val resultBuilder = new ValidationResultBuilder(limit)
    validatable.validateAgainst(xsd)
    validatable.setValidationProblemHandler(resultBuilder)
    resultBuilder
  }
}