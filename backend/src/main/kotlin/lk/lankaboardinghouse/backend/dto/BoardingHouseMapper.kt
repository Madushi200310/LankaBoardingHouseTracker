package lk.lankaboardinghouse.backend.dto

import lk.lankaboardinghouse.backend.model.BoardingHouse

/**
 * Single mapper for BoardingHouse → BoardingHouseResponseDto, shared between
 * BoardingHouseController and FavouriteController so the two endpoints can
 * never drift apart.
 */
object BoardingHouseMapper {

    fun toDto(b: BoardingHouse) = BoardingHouseResponseDto(
        id = b.id,
        title = b.title,
        description = b.description,
        rulesAndRegulations = b.rulesAndRegulations,
        price = b.price,
        addressLine = b.addressLine,
        townId = b.town.id,
        townName = b.town.name,
        districtId = b.town.district.id,
        districtName = b.town.district.name,
        ownerName = b.owner.fullName,
        ownerPhone = b.owner.phoneNumber,
        status = b.status,
        declineReason = b.declineReason,
        available = b.available
    )
}