package com.example.valomobile.data.remote.model

import com.google.gson.annotations.SerializedName

data class RiotStorefrontRawResponse(
    @SerializedName("FeaturedBundle") val featuredBundle: RiotFeaturedBundleWrapper? = null,
    @SerializedName("SkinsPanelLayout") val skinsPanelLayout: RiotSkinsPanelLayout? = null,
    @SerializedName("BonusStore") val bonusStore: RiotBonusStore? = null
)

data class RiotSkinsPanelLayout(
    @SerializedName("SingleItemOffers") val singleItemOffers: List<String> = emptyList(),
    @SerializedName("SingleItemOffersRemainingDurationInSeconds") val remainingDurationInSeconds: Long = 86400,
    @SerializedName("SingleItemStoreOffers") val singleItemStoreOffers: List<RiotSingleStoreOffer> = emptyList()
)

data class RiotSingleStoreOffer(
    @SerializedName("OfferID") val offerId: String,
    @SerializedName("Cost") val cost: Map<String, Int>? = null
)

data class RiotFeaturedBundleWrapper(
    @SerializedName("Bundle") val bundle: RiotRawBundle? = null,
    @SerializedName("Bundles") val bundles: List<RiotRawBundle> = emptyList(),
    @SerializedName("BundleRemainingDurationInSeconds") val bundleRemainingDurationInSeconds: Long = 86400
)

data class RiotRawBundle(
    @SerializedName("ID") val id: String? = null,
    @SerializedName("DataAssetID") val dataAssetId: String? = null,
    @SerializedName("DurationRemainingInSeconds") val durationRemainingInSeconds: Long = 86400,
    @SerializedName("TotalBaseCost") val totalBaseCost: Map<String, Int>? = null,
    @SerializedName("TotalDiscountedCost") val totalDiscountedCost: Map<String, Int>? = null,
    @SerializedName("TotalDiscountPercent") val totalDiscountPercent: Float = 0f,
    @SerializedName("ItemOffers") val itemOffers: List<RiotRawBundleItemOffer> = emptyList(),
    @SerializedName("Items") val items: List<RiotRawBundleItemOffer> = emptyList()
)

data class RiotRawBundleItemOffer(
    @SerializedName("BundleItemOfferID") val bundleItemOfferId: String? = null,
    @SerializedName("Offer") val offer: RiotRawBundleOffer? = null,
    @SerializedName("DiscountPercent") val discountPercent: Float = 0f,
    @SerializedName("DiscountedCost") val discountedCost: Map<String, Int>? = null,
    @SerializedName("BasePrice") val basePrice: Int? = null,
    @SerializedName("DiscountedPrice") val discountedPrice: Int? = null,
    @SerializedName("Item") val item: RiotRawReward? = null,
    @SerializedName("ItemID") val itemId: String? = null
)

data class RiotRawBundleOffer(
    @SerializedName("OfferID") val offerId: String? = null,
    @SerializedName("Cost") val cost: Map<String, Int>? = null,
    @SerializedName("Rewards") val rewards: List<RiotRawReward> = emptyList()
)

data class RiotRawReward(
    @SerializedName("ItemTypeID") val itemTypeId: String? = null,
    @SerializedName("ItemID") val itemId: String? = null
)

data class RiotBonusStore(
    @SerializedName("BonusStoreOffers") val bonusStoreOffers: List<RiotBonusStoreOffer> = emptyList(),
    @SerializedName("BonusStoreRemainingDurationInSeconds") val remainingDurationInSeconds: Long = 0
)

data class RiotBonusStoreOffer(
    @SerializedName("BonusOfferID") val bonusOfferId: String? = null,
    @SerializedName("Offer") val offer: RiotRawBundleOffer? = null,
    @SerializedName("DiscountPercent") val discountPercent: Int = 0,
    @SerializedName("DiscountCosts") val discountCosts: Map<String, Int>? = null
)

data class RiotWalletRawResponse(
    @SerializedName("Balances") val balances: Map<String, Int> = emptyMap()
)

data class UserWallet(
    val vp: Int = 0,
    val radianite: Int = 0,
    val kingdomCredits: Int = 0
)
