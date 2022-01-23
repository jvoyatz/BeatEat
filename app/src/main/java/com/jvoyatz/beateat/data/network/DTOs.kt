package com.jvoyatz.beateat.data.network
import com.jvoyatz.beateat.domain.Location
import com.jvoyatz.beateat.domain.Place
import com.jvoyatz.beateat.domain.PlaceCategory
import com.squareup.moshi.Json


fun List<ResultDTO>.toDomainModels(): List<Place>{
    return this.map {
        it.toDomainModel()
    }
}

fun ResultDTO.toDomainModel(): Place {
    return  Place(
        categories = this.categories.toCategories(),
        distance = this.distance,
        fsqId = this.fsqId,
        location = this.location.toDomainModel(this.geocodes.main.latitude, this.geocodes.main.longitude),
        name = this.name,
        placePhotoUrl = this.placePhotoUrl
    )
}
fun LocationDTO.toDomainModel(latitude: Double, longitude: Double): Location =
    Location(
        address = this.address,
        country = this.country,
        crossStreet = this.crossStreet,
        locality = this.locality,
        postcode = this.postcode,
        region = this.region,
        latitude = latitude,
        longitude = longitude
    )
fun CategoryDTO.toDomainModel(): PlaceCategory =
    PlaceCategory(id= this.id, name = this.name, icon= "${this.icon.prefix}44${this.icon.suffix}")

fun List<CategoryDTO>.toCategories(): List<PlaceCategory> = this.map {
    dto -> dto.toDomainModel()
}

data class PlaceResultsDTO(
    @Json(name = "context")
    val context: ContextDTO = ContextDTO(),
    @Json(name = "results")
    val results: List<ResultDTO> = listOf()
)

data class ContextDTO(
    @Json(name = "geo_bounds")
    val geoBounds: GeoBoundsDTO = GeoBoundsDTO()
)

data class GeoBoundsDTO(
    @Json(name = "circle")
    val circle: CircleDTO = CircleDTO()
)

data class CircleDTO(
    @Json(name = "center")
    val center: CenterDTO = CenterDTO(),
    @Json(name = "radius")
    val radius: Int = 0
)
data class CenterDTO(
    @Json(name = "latitude")
    val latitude: Double = 0.0,
    @Json(name = "longitude")
    val longitude: Double = 0.0
)

data class ResultDTO(
    @Json(name = "categories")
    val categories: List<CategoryDTO> = listOf(),
    @Json(name = "chains")
    val chains: List<Any> = listOf(),
    @Json(name = "distance")
    val distance: Int = 0,
    @Json(name = "fsq_id")
    val fsqId: String = "",
    @Json(name = "geocodes")
    val geocodes: GeocodesDTO = GeocodesDTO(),
    @Json(name = "location")
    val location: LocationDTO = LocationDTO(),
    @Json(name = "name")
    val name: String = "",
    @Json(name = "related_places")
    val relatedPlaces: RelatedPlacesDTO = RelatedPlacesDTO(),
    @Json(name = "timezone")
    val timezone: String = ""
){
    var placePhotoUrl: String? = ""
}

data class CategoryDTO(
    @Json(name = "icon")
    val icon: IconDTO = IconDTO(),
    @Json(name = "id")
    val id: Int = 0,
    @Json(name = "name")
    val name: String = ""
)

data class GeocodesDTO(
    @Json(name = "main")
    val main: MainDTO = MainDTO()
)

data class LocationDTO(
    @Json(name = "address")
    val address: String = "",
    @Json(name = "country")
    val country: String = "",
    @Json(name = "cross_street")
    val crossStreet: String = "",
    @Json(name = "locality")
    val locality: String = "",
    @Json(name = "postcode")
    val postcode: String = "",
    @Json(name = "region")
    val region: String = ""
)

class RelatedPlacesDTO

data class IconDTO(
    @Json(name = "prefix")
    val prefix: String = "",
    @Json(name = "suffix")
    val suffix: String = ""
)

data class MainDTO(
    @Json(name = "latitude")
    val latitude: Double = 0.0,
    @Json(name = "longitude")
    val longitude: Double = 0.0
)



//place details--photos

data class PlacePhotoDTO(
    @Json(name = "classifications")
    val classifications: List<String> = listOf(),
    @Json(name = "created_at")
    val createdAt: String = "",
    @Json(name = "height")
    val height: Int = 0,
    @Json(name = "id")
    val id: String = "",
    @Json(name = "prefix")
    val prefix: String = "",
    @Json(name = "suffix")
    val suffix: String = "",
    @Json(name = "width")
    val width: Int = 0
)