package org.kopi.galite.tests.report

import org.jetbrains.exposed.sql.Table
import org.junit.Test
import org.kopi.galite.chart.Chart
import org.kopi.galite.domain.Domain
import org.kopi.galite.form.dsl.Form
import org.kopi.galite.tests.JApplicationTestBase
import java.util.Locale
import kotlin.test.assertEquals
import org.kopi.galite.cross.VReportSelectionForm
import org.kopi.galite.form.dsl.KeyCode

class GenerateReportTests : JApplicationTestBase() {
  @Test
  fun sourceFormTest() {
    val formModel = TestReportForm.model
    assertEquals(TestReportForm::class.qualifiedName!!.replace(".", "/"), formModel.source)
  }
}

object User: Table() {
  val id = integer("id")
  val name = varchar("name", 20)
  val age = integer("age")
}

object TestReportForm: Form() {
  override val locale = Locale.FRANCE
  override val title = "Generate report"

  val report = actor (
          ident =  "report",
          menu =  "Action",
          label = "Report for test",
          help =  "Generate report" ,
          key  =  KeyCode.F1
  ) {
    icon =  "report"
  }

  init {
    page("test page") {
      menu("Action")
      val testBlock = block(1, 1, "Test", "Test block") {
        val u = table(User)
        val i = index(message = "ID should be unique")

        val id = hidden(domain = Domain<Int>(20)) {
          label = "id"
          help = "The user id"
          columns(u.id)
        }
        val name = mustFill(domain = Domain<String>(20), position = at(1, 1)) {
          label = "name"
          help = "The user name"
          columns(u.name)
        }
        val age = visit(domain = Domain<Int>(3), position = follow(name)) {
          label = "age"
          help = "The user age"
          columns(u.age) {
            index = i
            priority = 1
          }
        }

        command {
          item = report
          this.name = "report"
          action = {
            println("Report test -----------00000---------------")
            println("Report test -----------111111---------------")

         //   protected VReport createReport() throws VException {
         //     return new DeclarationBiensR(getForm(), @S.DateDebut, @S.DateFin, @!S.France, @!S.CEE, @!S.Monde);
         //   }
          }
        }
      }
    }
  }
}

class CommandesC(fournisseur: Int?): Chart() {
  override val title: String = "Fournisseur"
}
