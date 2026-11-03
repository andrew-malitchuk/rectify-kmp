# Dependency injection

Koin, assembled once in `compose-application`'s `initKoin()` and started from
`desktop-application`'s `main()`.

## The graph

```kotlin
public fun initKoin(config: KoinAppDeclaration = {}) {
    startKoin {
        config()
        modules(
            dataPreferenceImplModule,
            dataDatabaseImplModule,
            dataDeviceImplModule,
            dataRepositoryImplModule,
            domainUseCaseImplModule,
            presentationFeatureMirrorModule,
        )
    }
}
```

The order reads **data → domain → presentation**. Koin resolves lazily, so the order is
defensive rather than strictly load-bearing — but it documents the dependency direction at the
one place where the whole graph is visible, and keeping it is free.

## Why main() starts it

There is no lifecycle callback before `main()` on the desktop, so the entry point is the only
place Koin can be started.

`initKoin()` deliberately does **not** live inside `runDesktopApp()`. Bootstrapping DI is the
entry point's job, and `runDesktopApp()` is shared code that a test or a second host could call
without wanting a second `startKoin`.

## The DI convention plugin

`dev.rectify.io.convention.di` adds Koin dependencies and nothing else — no plugin IDs, no
targets. It must be applied **alongside** `library` or `feature`; on its own the configurations
it writes to do not exist yet.

`koin-core` is exposed as `api()` because consumers need it at compile time to call `get()` or
`koinInject()`. `koin-compose` and `koin-viewmodel` are `implementation()` — a module that
needs them applies the plugin itself.

## Qualifiers

`DeviceCommandExecutor` and `MirrorTransport` exist once per *mirrored device platform*, so
they are **always** qualified:

```kotlin
single<DeviceCommandExecutor>(named(ANDROID_SOURCE)) { AdbDeviceCommandExecutor(tools = get()) }
single<DeviceCommandExecutor>(named(IOS_SOURCE)) { IosDeviceCommandExecutor(tools = get(), hosts = get()) }
single<MirrorTransport>(named(ANDROID_SOURCE)) { ScreencapMirrorTransport(executor = get(named(ANDROID_SOURCE))) }
single<MirrorTransport>(named(IOS_SOURCE)) { ScreencapMirrorTransport(executor = get(named(IOS_SOURCE))) }
```

There is deliberately **no unqualified binding** of either to fall back on. A caller that
forgets the qualifier fails loudly instead of silently resolving whichever platform happened to
be registered last.

The constants live in `data-device-api` so the registration and the consumer cannot spell them
differently.

Note that the two transports are the *same class* twice, differing only in which executor they
were handed. `ScreencapMirrorTransport` is reused for iOS, not duplicated.

## The one test that makes this safe

A missing registration is a runtime failure that compiling cannot catch, and the failure lands
on the very first frame. `DeviceSourcesModuleTest` resolves every binding the graph will be
asked for at launch:

```kotlin
@Test
fun everyDeviceDatasourceResolvesOnDesktop() {
    assertEveryRegistrationResolves(koinFor(dataDeviceImplModule))
}

@Test
fun noUnqualifiedExecutorOrTransportResolves() {
    val koin = koinFor(dataDeviceImplModule)
    assertNull(koin.getOrNull<DeviceCommandExecutor>())
    assertNull(koin.getOrNull<MirrorTransport>())
}
```

Resolution only — constructing these spawns nothing. It earned its place the hard way: a wave
of development once left the repository asking for bindings nobody registered, and the whole
suite stayed green because nothing resolved the graph.

The second test is the other half of the contract. Without it, an unqualified binding could
reappear and every "forgot the qualifier" bug would go back to silently working.
