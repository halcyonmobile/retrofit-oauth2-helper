# Starting of an oauth extension for retrofit

## Purpose

The purpose of this library is to unify our oauth2 implementation and ease it to integrate with our backend.
Still it tries to be as flexible as possible to be able to use it even if some parameters, paths etc is different.
The default implementation are using the most common configurations, and the [app] and [core] module contains examples how to integrate the app.

## Ouath2 in a nutshell

Oauth2 is a protocol for authorization.


In a nutshell for Android developers this means you will have two kinds of request, one with session and one without it.


- The one without session will need to contain a header with a clientId. 
- The one with session will need to contain a header with a token.

 
You get the token when you login / signup.
These token can expire, meaning they are no longer usable after a certain amount of time.


When you get the tokens you get two kinds: access token and refresh token:
- The access token has to be attached to the requests.
- The refresh token is used to get a new access token.

 
When the access token expires you will get 401 Unauthorized from the server, at this point you will need to call a request with the refresh token, from that request you will get a new access and refresh token.

 
With retrofit there is a class Authenticator which is triggered on a background thread when your request fails with 401.
At this point you can call the refresh api and update the request which failed, then it will be retried.

 
Details can be found [here](https://oauth.net/2/ "here")

 
Blog post explaining it [here](http://www.bubblecode.net/en/2016/01/22/understanding-oauth2/#targetText=OAuth2%20is%2C%20you%20guessed%20it,access%20on%20its%20own%20behalf. "here")


### So how does this library help you with that?

The library adds an authenticator implementation to retrofit with session, meaning it will call the refresh request for you.
The library adds the proper headers to your requests based on if it were created with session or sessionless retrofit.


Error Cases:

- When the refresh-token is no longer valid or expired, (returned by the server while trying to refresh token) then you will receive a callback with session expiration and your request will fail with 401.
- When the refresh-token request failed 3 times, then your request will fail with 401. And should be handled as normal network error
 
 
To see the behaviour in action may refer to [com.halcyonmobile.core.AuthenticationTest] 
 
## Setup
- This contains how you can use this library
- Latest version: 1.0.0

### Make sure jcenter is added

### General Idea

- You will see a "core" and "app" module, this is specific to our architecture, the core means a module which does the business logic, network requests etc, it's a java module while the app module is handling the ui and other platform specific implementation details.
- The idea is that in your core module you will do the configuration and get the created retrofit instances 
so it will depend on either the oauth or oauthkoin, oauthmoshi or some other variant
- However the core module won't be able to contain all the needed dependencies, because of that you should use the 
oauthdependencies in your app module so you can provide the storage and session expiration handler.
- Optionally you can use the oauthstorage in your app to reduce the shared preferences boilerplate.
- Optionally you can use the oauthadaptergenerator in your core where you define the refresh token retrofit service, so 
you don't need to write your adapter if it's simple.
- Note: oauth-moshi, oauth-koin do not need adapters, they already contain a refresh service.

### Oauth-moshi setup
If you are using moshi and some other dependency injection framework than koin what you need to do is add the dependency in your build.gradle of your core module
Note: still the example will be using koin, to adapt to your DI is your responsibility.

```groovy
implementation "com.halcyonmobile.oauth-setup:oauth-setup-moshi:latest-version"
```

Then add the module to your other core modules, the setup will look something like this:
```kotlin
fun createNetworkModules(
    clientId: String,
    baseUrl: String,
    provideAuthenticationLocalStorage: Scope.() -> AuthenticationLocalStorage,
    provideSessionExpiredEventHandler: Scope.() -> SessionExpiredEventHandler
): List<Module> {
    return listOf(
        module {
            factory { get<Retrofit>(SESSION_RETROFIT).create(SessionExampleService::class.java) }
            factory { get<Retrofit>(NON_SESSION_RETROFIT).create(SessionlessExampleService::class.java) }
            factory { ExampleRemoteSource(get(), get()) }
        },
        module {
            single { provideAuthenticationLocalStorage() }
            single { provideSessionExpiredEventHandler() }
            single {
                OauthRetrofitWithMoshiContainerBuilder(
                    clientId = clientId,
                    authenticationLocalStorage = provideAuthenticationLocalStorage(),
                    sessionExpiredEventHandler = provideSessionExpiredEventHandler()
                )
                    .configureRetrofit {
                        baseUrl(baseUrl)
                    }
                    .build()
            }
            single(SESSION_RETROFIT) { get<OauthRetrofitContainerWithMoshi>().oauthRetrofitContainer.sessionRetrofit }
            single(NON_SESSION_RETROFIT) { get<OauthRetrofitContainerWithMoshi>().oauthRetrofitContainer.sessionlessRetrofit }
            single { get<OauthRetrofitContainerWithMoshi>().moshi }
        }
    )
}
```

#### app module

If you want to save your session in shared preferences may use oauthstorage, in this case:
in your build.gradle of your app module add the following dependency:
```groovy
implementation "com.halcyonmobile.oauth-setup:oauth-setup-storage:latest-version"
```

Extend your shared preferences manager from the AuthenticationSharedPreferencesStorage
```kotlin
class SharedPreferenceManager(private val sharedPreferences: SharedPreferences) : AuthenticationSharedPreferencesStorage(sharedPreferences),
```

Implement the com.halcyonmobile.oauth.depencencies.SessionExpiredEventHandler.
And tie the setup together such as:
```kotlin
fun createAllModules(baseUrl: String, clientId: String): List<Module> {
    return listOf(createAppModule(omegaApplication))
    .plus(createNetworkModules(
        baseUrl = baseUrl,
        clientId = clientId,
        provideAuthenticationLocalStorage = { get<SharedPreferenceManager>() },
        provideSessionExpiredEventHandler = { get<SessionExpiredEventHandlerImpl>() }
    ))
}
```

Note: if you are not saving your session into shared preferences, instead of 'oauth-setup-storage' dependency, use 'oauth-setup-dependencies'.
You implement the AuthenticationLocalStorage interface with your solution and add it to your createNetworkModule setup instead of SharedPreferencesManager.

#### LOGIN and SIGNUP requests
For your login and signup requests, you still have to save the session yourself into your storage.
The easiest solution is return the same session type, inject the AuthenticationLocalStorage and simply call save on it.

Note: There is an idea with call adapter which would save your session automatically, but it's not yet implemented.
Feel free to ping me if you are interested in this.

#### I have a request which contains the access / refresh token, What can I do?

For this there is a specific header which when attached after authentication is finished successfully a specific exception is thrown so you can rerun your request with the updated content.

```kotlin
@GET("test/service")
fun authInvalidTest(
    @Header(INVALIDATION_AFTER_REFRESH_HEADER_NAME) invalidHeader : String = INVALIDATION_AFTER_REFRESH_HEADER_VALUE
) : Call<Unit>
// throws authFinishedInvalidationException, which is an IOException after authentication happened
```

How to handle the exception:
```kotlin
    fun foo(){
        runCatchingCausedByAuthFinishedInvalidation({
            service.authInvalidTest()
        }, {
            // authentication happened, the storage is updated
            // do something, like retrying the request with updated body
        })
    }
```

### Oauth-gson setup
If you are using moshi and some other dependency injection framework than koin what you need to do is add the dependency in your build.gradle of your core module
Note: still the example will be using koin, to adapt to your DI is your responsibility.

```groovy
implementation "com.halcyonmobile.oauth-setup:oauth-setup-gson:latest-version"
```

Then add the module to your other core modules, the setup will look something like this:
```kotlin
fun createNetworkModules(
    clientId: String,
    baseUrl: String,
    provideAuthenticationLocalStorage: Scope.() -> AuthenticationLocalStorage,
    provideSessionExpiredEventHandler: Scope.() -> SessionExpiredEventHandler
): List<Module> {
    return listOf(
        module {
            factory { get<Retrofit>(SESSION_RETROFIT).create(SessionExampleService::class.java) }
            factory { get<Retrofit>(NON_SESSION_RETROFIT).create(SessionlessExampleService::class.java) }
            factory { ExampleRemoteSource(get(), get()) }
        },
        module {
            single { provideAuthenticationLocalStorage() }
            single { provideSessionExpiredEventHandler() }
            single {
                OauthRetrofitWithGsonContainerBuilder(
                    clientId = clientId,
                    authenticationLocalStorage = provideAuthenticationLocalStorage(),
                    sessionExpiredEventHandler = provideSessionExpiredEventHandler()
                )
                    .configureRetrofit {
                        baseUrl(baseUrl)
                    }
                    .build()
            }
            single(SESSION_RETROFIT) { get<OauthRetrofitContainerWithGson>().oauthRetrofitContainer.sessionRetrofit }
            single(NON_SESSION_RETROFIT) { get<OauthRetrofitContainerWithGson>().oauthRetrofitContainer.sessionlessRetrofit }
            single { get<OauthRetrofitContainerWithGson>().gson }
        }
    )
}
```

#### app module

Same as Oauth-moshi setup, please check that one out.

### Koin with moshi

#### core module
If you are using koin with moshi what you need to do is add the dependency in your build.gradle of your core module

```groovy
implementation "com.halcyonmobile.oauth-setup:oauth-setup-moshi-koin:latest-version"
```

Then add the module to your other core modules, the setup will look something like this:
```kotlin
fun createNetworkModules(
    clientId: String,
    baseUrl: String,
    provideAuthenticationLocalStorage: Scope.() -> AuthenticationLocalStorage,
    provideSessionExpiredEventHandler: Scope.() -> SessionExpiredEventHandler
): List<Module> {
    return listOf(
    // your own custom module,
        module {
            factory { get<Retrofit>(SESSION_RETROFIT).create(SessionExampleService::class.java) }
            factory { get<Retrofit>(NON_SESSION_RETROFIT).create(SessionlessExampleService::class.java) }
            factory { ExampleRemoteSource(get(), get()) }
        },
        // this returns a koin module. here you can customize the setup.
        createOauthModule(
            clientId = clientId,
            provideSessionExpiredEventHandler = provideSessionExpiredEventHandler,
            provideAuthenticationLocalStorage = provideAuthenticationLocalStorage,
            configureRetrofit = {
                it.baseUrl(baseUrl)
            }
        )
    )
}
```

#### app module
Same as Oauth-moshi setup, please check that one out.

### Using Only oauth setup
If none of the other setups are applicable, you are not using moshi then you can fallback to this, however i would suggest to add a new module with your implementation instead.
Note: still the example will be using koin and moshi, to adapt to your DI is your responsibility. 

#### core module

```groovy
implementation "com.halcyonmobile.oauth-setup:oauth-setup:latest-version"
// optional
implementation "com.halcyonmobile.oauth-setup:oauth-adapter-generator:latest-version"
```

Create your DTO for the session, example using moshi:
```kotlin
@JsonClass(generateAdapter = true)
data class RefreshTokenResponsex(
    @field:Json(name = "user_id") override val userId: String,
    @field:Json(name = "access_token") override val token: String,
    @field:Json(name = "refresh_token") override val refreshToken: String,
    @field:Json(name = "token_type") override val tokenType: String
) : SessionDataResponse
```

Create your refresh token service, example of it:
```kotlin
@RefreshService // optional, needed if you use the oauth-adapter-generator
interface RefreshTokenService {

    @POST("oauth/token")
    @FormUrlEncoded
    fun refresh(@Field("refresh_token") refreshToken: String, @Field("grant_type") grantType: String = "refresh_token"): Call<RefreshTokenResponsex>
}
```

If you choose not to use the annotation processor or you are unable to because of some customization, you will have to create your own adapter
Example what the annotation processor generates:
```kotlin
/**
 * [AuthenticationServiceAdapter] implementation generated for
     [com.halcyonmobile.core.RefreshTokenService] class annotated with
        [com.halcyonmobile.oauth.dependencies.RefreshService] */
internal class RefreshTokenServiceAuthenticationServiceAdapter :
        AuthenticationServiceAdapter<RefreshTokenService> {
    override fun adapt(service: RefreshTokenService): AuthenticationService =
            AuthenticationServiceImpl(service)

    class AuthenticationServiceImpl(private val service: RefreshTokenService) :
            AuthenticationService {
        override fun refreshToken(refreshToken: String): Call<out SessionDataResponse> =
                service.refresh(refreshToken)
    }
}
```

Then add the module to your other core modules, the setup will look something like this:
```kotlin
fun createNetworkModules(
    clientId: String,
    baseUrl: String,
    provideAuthenticationLocalStorage: Scope.() -> AuthenticationLocalStorage,
    provideSessionExpiredEventHandler: Scope.() -> SessionExpiredEventHandler
): List<Module> {
    return listOf(
        module {
            factory { get<Retrofit>(SESSION_RETROFIT).create(SessionExampleService::class.java) }
            factory { get<Retrofit>(NON_SESSION_RETROFIT).create(SessionlessExampleService::class.java) }
            factory { ExampleRemoteSource(get(), get()) }
        },
        module {
            single { provideAuthenticationLocalStorage() }
            single { provideSessionExpiredEventHandler() }
            single { Moshi.Builder().build() }
            single {
                OauthRetrofitContainerBuilder(
                    clientId = clientId,
                    refreshServiceClass = RefreshTokenService::class,
                    authenticationLocalStorage = provideAuthenticationLocalStorage(),
                    sessionExpiredEventHandler = provideSessionExpiredEventHandler(),
                    adapter = RefreshTokenServiceAuthenticationServiceAdapter()
                )
                    .configureRetrofit {
                        baseUrl(baseUrl).addConverterFactory(MoshiConverterFactory.create(get()))
                    }
                    .build()
            }
            single(SESSION_RETROFIT) { get<OauthRetrofitContainer>().sessionRetrofit }
            single(NON_SESSION_RETROFIT) { get<OauthRetrofitContainer>().sessionlessRetrofit }
        }
    )
}
```

#### app module

Same as oauth-moshi-koin setup, please check that one out.

### Configurations:

For any kind of configuration there is a function in the builder class. To see more specifically, please refer the documentation of the used builder.

The basic ones are the following:

- What should be considered SessionExpiration
- Configure the okhttp (both, sessionless, session), adding logger, timeout changes etc.
- Configure the retrofit, adding baseurl, parser etc.


For parsers there are more configuration:

- Configuring the moshi or gson
- Configuring the service path used
- Configuring the grantType value
- Configuring the parameter name of the refresh token send with the refresh token request
- disabling the default parsing (in this case the user is responsibil for the parsing)
- add addinitonal parameters to the refresh token service

## Structure
The following section describes current modules and the preferred content & usage

### app
- An example how can you include this into your app

### core
- An example of a core layer which used for networking and how it needs to configure the retrofit instances

### oauth
- The base implementation of the extension. 

### oauthdependencies
- Dependencies which has to come from the outside (app module), but has no relation to retrofit

### oauthkoin
- this module contains configuration functions which create the koin modules you can simply add to your startKoin method
- uses koin 1.0.2

### oauthdagger (PLANED)
- WIP

### oauthstorage
- A persistent storage for session based on SharedPreferences. It's implemented in a way that can be used separately or 
with an existing SharedPreferencesManager

### oauthsecurestorage
- A persistent storage for session based on EncryptedSharedPreferences. It's implemented in a way that can be used separately or
with an existing SharedPreferencesManager
- min API 23

### oauthsecurestoragecompat
- A persistent storage for session based on EncryptedSharedPreferences above API 23 and SharedPreferences below. It's implemented in a way that can be used separately or
with an existing SharedPreferencesManager

### oauthadaptergenerator
- An optional annotation processor which tries to reduce boilerplate even more.
You may use it if you don't use a version which does the parsing for you.

### oauthparsing
- An extension of the base implementation which includes the service and interfaces for other modules which do the actual parsing of the session.

### oauthmoshi
- An extension of the base implementation which includes moshi and the service with default parameters
Here you don't need to write your own service and parsing, however you are still able to configure the service and parsing.

Note: it implements the oauthparsing

### oauthmoshkoin
- this module contains a configuration function which create the koin module, which you can simply add to your other modules
- uses koin 2.0.1

### oauthgson
- An extension of the base implementation which includes gson and the service with default parameters
Here you don't need to write your own service and parsing, however you are still able to configure the service and parsing.

Note: it implements the oauthparsing

<h1 id="license">License :page_facing_up:</h1>

Copyright (c) 2020 Halcyon Mobile.
> https://www.halcyonmobile.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

> http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
