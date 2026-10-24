package ext

import kotlinx.serialization.json.Json

internal val customJson = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    isLenient = true
}
