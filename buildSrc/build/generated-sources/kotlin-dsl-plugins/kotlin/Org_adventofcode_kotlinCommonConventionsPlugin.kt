/**
 * Precompiled [org.adventofcode.kotlin-common-conventions.gradle.kts][Org_adventofcode_kotlin_common_conventions_gradle] script plugin.
 *
 * @see Org_adventofcode_kotlin_common_conventions_gradle
 */
class Org_adventofcode_kotlinCommonConventionsPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Org_adventofcode_kotlin_common_conventions_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
