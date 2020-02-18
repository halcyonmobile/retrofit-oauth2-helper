/*
 * Copyright (c) 2020 Halcyon Mobile.
 * https://www.halcyonmobile.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.halcyonmobile.oauthadaptergenerator

import com.halcyonmobile.oauth.dependencies.RefreshService
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import java.io.File
import java.io.IOException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

/**
 * Annotation Processor which generated [com.halcyonmobile.oauth.AuthenticationServiceAdapter]
 * The generated adapter should be used in the [com.halcyonmobile.oauth.OauthRetrofitContainerBuilder]
 *
 * Example:
 *
 * Given the following file:
 * ```
 * @RefreshService
 * interface RefreshTokenService {
 *
 * @POST("oauth/token")
 * @FormUrlEncoded
 * fun refresh(@Field("refresh_token") refreshToken: String, @Field("grant_type") grantType: String = "refresh_token"): Call<RefreshTokenResponse>
 * }
 * ```
 *
 * Where RefreshTokenResponse looks lke this:
 * ```
 * data class RefreshTokenResponse(
 *   override val userId: String,
 *   override val token: String,
 *   override val refreshToken: String,
 *   override val tokenType: String
 * ) : SessionDataResponse
 * ```
 *
 * Will generate the following adapter:
 * ```
 * package com.halcyonmobile.oauth
 *
 * import com.halcyonmobile.core.RefreshTokenService
 * import kotlin.String
 * import retrofit2.Call
 *
 * /**
 *  * [AuthenticationServiceAdapter] implementation generated for
 *  * [com.halcyonmobile.core.RefreshTokenService] class annotated with [com.halcyonmobile.oauth.dependencies.RefreshService]
 *  */
 * class RefreshTokenServiceAuthenticationServiceAdapter :
 * AuthenticationServiceAdapter<com.halcyonmobile.core.RefreshTokenService> {
 *
 * override fun adapt(service: RefreshTokenService): AuthenticationService = AuthenticationServiceImpl(service)
 *
 *   class AuthenticationServiceImpl(private val service: RefreshTokenService) : AuthenticationService {
 *
 *     override fun refreshToken(refreshToken: String): Call<out SessionDataResponse> = service.refresh(refreshToken)
 *   }
 * }
 * ```
 */
class Processor : AbstractProcessor() {

    private lateinit var processingEnvironment: ProcessingEnvironment
    private lateinit var messager: Messager
    private lateinit var elementUtils: Elements

    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        this.processingEnvironment = processingEnvironment
        messager = processingEnvironment.messager
        elementUtils = processingEnvironment.elementUtils
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        val set = HashSet<String>()
        set.add(RefreshService::class.java.canonicalName)
        return set
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        try {
            val annotatedElements = roundEnvironment.getElementsAnnotatedWith(RefreshService::class.java)
            showLogsForInvalidAnnotatedElements(annotatedElements)
            val types = annotatedElements.filterIsInstance<TypeElement>()
            showLogsForInvalidClasses(types)

            processFilteredElements(types.filter { it.enclosedElements.filterIsInstance<ExecutableElement>().count() == 1 })

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }

    private fun showLogsForInvalidAnnotatedElements(elements: Set<Element>) {
        elements.filter { element -> element.kind != ElementKind.INTERFACE }
            .forEach {
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Can only be applied to interfaces and ${it.simpleName} is not an interface"
                )
            }
    }

    private fun processFilteredElements(types: List<TypeElement>) {
        types.map(::InterfaceToProcess)
            .map { it.toFileSpec() }
            .forEach { file ->
                processingEnvironment.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]?.let(::File)?.let(file::writeTo)
            }
    }

    private fun showLogsForInvalidClasses(types: List<TypeElement>) {
        types.filterNot { it.enclosedElements.filterIsInstance<ExecutableElement>().count() == 1 }
            .forEach {
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Will not generate class for $it, because it contains too many functions"
                )
            }
    }

    private fun InterfaceToProcess.toFileSpec() =
        FileSpec.builder("com.halcyonmobile.oauth", "${simpleName}AuthenticationServiceAdapter")
            .addType(createServiceAdapter())
            .build()

    private fun InterfaceToProcess.createServiceAdapter(): TypeSpec {
        val classOfTypeVariableName = ClassName.bestGuess("com.halcyonmobile.oauth.AuthenticationServiceAdapter")
            .parameterizedBy(TypeVariableName(className.toString()))

        return TypeSpec.classBuilder("${simpleName}AuthenticationServiceAdapter")
            .addModifiers(KModifier.INTERNAL)
            .addSuperinterface(classOfTypeVariableName)
            .addKdoc("[AuthenticationServiceAdapter] implementation generated for [$className] class annotated with [${RefreshService::class.asClassName()}]")
            .addType(createAuthenticationServiceImpl())
            .addFunction(
                FunSpec.builder("adapt")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(ParameterSpec.builder("service", className).build())
                    .returns(ClassName.bestGuess("com.halcyonmobile.oauth.AuthenticationService"))
                    .addStatement("return AuthenticationServiceImpl(service)")
                    .build()
            )
            .build()
    }

    private fun InterfaceToProcess.createAuthenticationServiceImpl(): TypeSpec {
        val callType = ClassName.bestGuess("retrofit2.Call")
            .parameterizedBy(TypeVariableName("out SessionDataResponse"))

        return TypeSpec.classBuilder("AuthenticationServiceImpl")
            .addSuperinterface(ClassName.bestGuess("com.halcyonmobile.oauth.AuthenticationService"))
            .addFunction(
                FunSpec.builder("refreshToken")
                    .returns(callType)
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(
                        ParameterSpec.builder("refreshToken", String::class.asClassName())
                            .build()
                    )
                    .addStatement("return service.$functionName(refreshToken)")
                    .build()
            )
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(ParameterSpec.builder("service", className).build())
                    .build()
            )
            .addProperty(
                PropertySpec.builder("service", className)
                    .initializer("service")
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
            .build()
    }

    data class InterfaceToProcess(val simpleName: String, val functionName: String, val className: ClassName) {

        constructor(typeElement: TypeElement) : this(
            simpleName = typeElement.simpleName.toString(),
            functionName = typeElement.enclosedElements.filterIsInstance<ExecutableElement>().first().simpleName.toString(),
            className = typeElement.asClassName()
        )
    }

    companion object {
        private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}