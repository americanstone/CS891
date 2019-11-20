package admin

import edu.vanderbilt.imagecrawler.utils.Assignment.*
import org.junit.Assert.assertTrue
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * A testing rule will automatically set and reset the Assignment class flags for each test.
 */
class AssignmentTestRule : TestRule {
    override fun apply(base: Statement?, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                assertTrue(isGraduateTodo() || isUndergraduateTodo())
                val assignmentTypes = sTypes
                try {
                    base?.evaluate()
                } finally {
                    sTypes = assignmentTypes
                }
            }
        }
    }
}
