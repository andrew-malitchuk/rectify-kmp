package data.device.api.source.datasource

/**
 * Koin qualifier for the Android half of a two-platform interface.
 *
 * Here, in the API module, rather than beside the registrations: both the registrations
 * (`data-device-impl`) and the consumer (`data-repository-impl`'s `DeviceRepositoryImpl`
 * wiring) have to spell it the same way, and a qualifier typo is not a compile error — it is
 * a crash the first time the app resolves the graph, on whichever platform got it wrong.
 */
public const val ANDROID_SOURCE: String = "android"

/** Koin qualifier for the iOS half. @see ANDROID_SOURCE */
public const val IOS_SOURCE: String = "ios"
