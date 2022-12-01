/**
 * Precompiled [org.adventofcode.kotlin-application-conventions.gradle.kts][Org_adventofcode_kotlin_application_conventions_gradle] script plugin.
 *
 * @see Org_adventofcode_kotlin_application_conventions_gradle
 */
class Org_adventofcode_kotlinApplicationConventionsPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Org_adventofcode_kotlin_application_conventions_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
