import korlibs.applyProjectProperties

description = "Consistent and portable date and time utilities for multiplatform Kotlin"

//project.ext.props = [
//    "project.scm.url" : "https://github.com/korlibs/klock",
//    "project.license.name" : "CC0 1.0 Universal",
//    "project.license.url" : "https://raw.githubusercontent.com/korlibs/klock/master/LICENSE",
//    "project.author.id" : "soywiz",
//    "project.author.name" : "Carlos Ballesteros Velasco",
//    "project.author.email" : "soywiz@gmail.com",
//]

project.extensions.extraProperties.properties.apply {
    applyProjectProperties("https://github.com/korlibs/korge/main/kbignum",
        "CC0 1.0 Universal",
        "https://raw.githubusercontent.com/korlibs/klock/master/LICENSE"
        )
}
