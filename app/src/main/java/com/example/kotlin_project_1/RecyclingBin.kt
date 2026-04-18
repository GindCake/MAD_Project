package com.example.kotlin_project_1

enum class BinType {
    PAPER, GLASS, PLASTIC, ORGANIC, BATTERY, E_WASTE
}

data class RecyclingBin(
    val id: String,
    val type: BinType,
    val latitude: Double,
    val longitude: Double,
    val address: String
)

object CampusData {
    val exampleBins = listOf(
        // ETSI Informática
        RecyclingBin("1", BinType.PLASTIC, 40.4523, -3.7261, "ETSI Informática - Main Entrance"),
        RecyclingBin("2", BinType.PAPER, 40.4518, -3.7265, "ETSI Informática - South Wing"),
        RecyclingBin("3", BinType.ORGANIC, 40.4520, -3.7255, "ETSI Informática - Side Garden"),
        RecyclingBin("4", BinType.GLASS, 40.4528, -3.7268, "ETSI Informática - Parking North"),
        
        // ETSI Telecomunicación
        RecyclingBin("5", BinType.GLASS, 40.4528, -3.7258, "Near Teleco Entrance"),
        RecyclingBin("6", BinType.BATTERY, 40.4525, -3.7260, "Teleco Lab Area"),
        RecyclingBin("7", BinType.PAPER, 40.4532, -3.7250, "Teleco Library Entrance"),
        RecyclingBin("8", BinType.PLASTIC, 40.4535, -3.7262, "Teleco Courtyard"),
        
        // General Campus areas
        RecyclingBin("9", BinType.ORGANIC, 40.4530, -3.7270, "Campus Cafeteria"),
        RecyclingBin("10", BinType.BATTERY, 40.4515, -3.7275, "Maintenance Building"),
        RecyclingBin("11", BinType.PLASTIC, 40.4510, -3.7260, "Bus Stop Entrance"),
        RecyclingBin("12", BinType.PAPER, 40.4540, -3.7255, "Sports Area Entrance")
    )
}
