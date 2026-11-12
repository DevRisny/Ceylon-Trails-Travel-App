package com.risny.trails.core

import com.risny.trails.core.Category.ADVENTURE
import com.risny.trails.core.Category.BEACH
import com.risny.trails.core.Category.CITY
import com.risny.trails.core.Category.HERITAGE
import com.risny.trails.core.Category.HILLS
import com.risny.trails.core.Category.WILDLIFE

/**
 * Curated destinations. Best months follow the two monsoons: the south-west coast and hills are
 * driest around December-April, the east coast around May-September, the dry zone most of the year.
 * They are typical seasons, not guarantees.
 */
object Destinations {
    private val SOUTH_WEST = setOf(12, 1, 2, 3, 4)
    private val EAST = setOf(5, 6, 7, 8, 9)
    private val DRY_ZONE = (1..9).toSet()

    val ALL: List<Destination> = listOf(
        Destination("sigiriya", "Sigiriya", "Sigiriya", "Central", setOf(HERITAGE, ADVENTURE), 7.9570, 80.7603, DRY_ZONE,
            "A 5th-century fortress on top of a 200 m rock",
            listOf("Climb past the giant lion's paws", "Frescoes and the Mirror Wall", "Sunrise from Pidurangala Rock next door")),
        Destination("dambulla", "Dambulla Cave Temple", "Dambulla cave temple", "Central", setOf(HERITAGE), 7.8567, 80.6492, DRY_ZONE,
            "Cave shrines filled with Buddha statues and painted ceilings",
            listOf("Five caves of statues and murals", "UNESCO World Heritage Site", "Views over the plains from the rock")),
        Destination("polonnaruwa", "Polonnaruwa", "Polonnaruwa", "North Central", setOf(HERITAGE), 7.9403, 81.0188, DRY_ZONE,
            "The medieval royal capital, best explored by bicycle",
            listOf("Rock-carved Buddhas of Gal Vihara", "The Royal Palace ruins", "Parakrama Samudra reservoir at sunset")),
        Destination("anuradhapura", "Anuradhapura", "Anuradhapura", "North Central", setOf(HERITAGE), 8.3114, 80.4037, DRY_ZONE,
            "Sri Lanka's first great capital and a sacred city",
            listOf("Sri Maha Bodhi, the sacred fig tree", "The white dome of Ruwanwelisaya", "Ancient reservoirs and monasteries")),
        Destination("kandy", "Kandy", "Kandy", "Central", setOf(HERITAGE, CITY), 7.2936, 80.6413, SOUTH_WEST,
            "The hill capital and home of the Temple of the Tooth",
            listOf("Temple of the Sacred Tooth Relic", "A walk around Kandy Lake", "Peradeniya Botanical Gardens", "Esala Perahera in July / August")),
        Destination("ella", "Ella", "Ella, Sri Lanka", "Uva", setOf(HILLS, ADVENTURE), 6.8667, 81.0466, SOUTH_WEST,
            "Tea-covered hills, waterfalls and easy hikes",
            listOf("Trains crossing the Nine Arch Bridge", "Sunrise hike up Little Adam's Peak", "The Kandy-Ella train ride")),
        Destination("nuwara-eliya", "Nuwara Eliya", "Nuwara Eliya", "Central", setOf(HILLS), 6.9497, 80.7891, setOf(1, 2, 3, 4),
            "\"Little England\" among the tea estates",
            listOf("Tea factory tours", "Gregory Lake", "Colonial-era bungalows and gardens")),
        Destination("horton-plains", "Horton Plains", "Horton Plains National Park", "Central", setOf(HILLS, WILDLIFE, ADVENTURE), 6.8023, 80.8018, setOf(1, 2, 3, 4),
            "A cool highland plateau that ends at World's End",
            listOf("The World's End cliff walk (go early, before the mist)", "Baker's Falls", "Sambar deer on the grasslands")),
        Destination("adams-peak", "Adam's Peak", "Adam's Peak", "Sabaragamuwa", setOf(ADVENTURE, HERITAGE), 6.8096, 80.4994, setOf(12, 1, 2, 3, 4, 5),
            "The night pilgrimage climb to a sunrise above the clouds",
            listOf("Start around 2 am to reach the summit for sunrise", "The peak's shadow triangle at dawn", "Pilgrim season runs December to May")),
        Destination("haputale", "Haputale", "Haputale", "Uva", setOf(HILLS), 6.7656, 80.9590, SOUTH_WEST,
            "Ridge-top town with some of the widest views in the hills",
            listOf("Sunrise at Lipton's Seat", "Walks through Dambatenne tea estate", "Adisham Bungalow")),
        Destination("knuckles", "Knuckles Range", "Knuckles Mountain Range", "Central", setOf(ADVENTURE, HILLS), 7.4500, 80.8000, setOf(1, 2, 3, 4, 7, 8),
            "Cloud forests and remote villages for serious trekkers",
            listOf("Multi-day treks with a local guide", "Mini World's End viewpoint", "Waterfalls and river pools")),
        Destination("galle", "Galle Fort", "Galle Fort", "Southern", setOf(HERITAGE, CITY, BEACH), 6.0260, 80.2170, SOUTH_WEST,
            "A walled colonial town on the edge of the Indian Ocean",
            listOf("Sunset walk along the ramparts", "The lighthouse and old Dutch streets", "Cafés and boutiques in historic houses")),
        Destination("mirissa", "Mirissa", "Mirissa", "Southern", setOf(BEACH, WILDLIFE), 5.9483, 80.4716, setOf(11, 12, 1, 2, 3, 4),
            "A palm-lined bay famous for whale watching",
            listOf("Blue whale and dolphin boat trips", "Coconut Tree Hill", "Secret Beach")),
        Destination("unawatuna", "Unawatuna", "Unawatuna", "Southern", setOf(BEACH), 6.0100, 80.2490, SOUTH_WEST,
            "A sheltered, swimmable bay just south of Galle",
            listOf("Snorkelling on the reef", "Japanese Peace Pagoda", "Jungle Beach")),
        Destination("hikkaduwa", "Hikkaduwa", "Hikkaduwa", "Southern", setOf(BEACH, ADVENTURE), 6.1395, 80.1063, setOf(11, 12, 1, 2, 3, 4),
            "Surf breaks, coral and sea turtles",
            listOf("Hikkaduwa Coral Reef", "Swim with sea turtles near the shore", "Beginner and reef surf breaks")),
        Destination("bentota", "Bentota", "Bentota", "Southern", setOf(BEACH, ADVENTURE), 6.4210, 79.9980, SOUTH_WEST,
            "Long golden beaches and water sports on the river",
            listOf("Jet ski, banana boat and windsurfing", "Bentota River boat rides", "Brief Garden by Bevis Bawa")),
        Destination("yala", "Yala National Park", "Yala National Park", "Southern", setOf(WILDLIFE), 6.3725, 81.5160, setOf(2, 3, 4, 5, 6, 7),
            "Sri Lanka's best-known park for leopards",
            listOf("Early-morning jeep safari", "Leopards, elephants and sloth bears", "Lagoons full of water birds")),
        Destination("udawalawe", "Udawalawe", "Udawalawe National Park", "Sabaragamuwa", setOf(WILDLIFE), 6.4740, 80.8987, (1..12).toSet(),
            "Herds of elephants, almost guaranteed all year",
            listOf("Elephant herds around the reservoir", "Elephant Transit Home feeding time", "Raptors and water birds")),
        Destination("minneriya", "Minneriya", "Minneriya National Park", "North Central", setOf(WILDLIFE), 8.0367, 80.8333, setOf(7, 8, 9),
            "The Gathering: hundreds of elephants at one reservoir",
            listOf("The dry-season elephant gathering", "Evening jeep safari", "Combine with Sigiriya and Polonnaruwa")),
        Destination("wilpattu", "Wilpattu", "Wilpattu National Park", "North Western", setOf(WILDLIFE), 8.4500, 80.0300, (2..9).toSet(),
            "The largest national park: quiet, wild and lake-dotted",
            listOf("Natural lakes (villus) through the forest", "Leopards and sloth bears", "Far fewer jeeps than Yala")),
        Destination("trincomalee", "Trincomalee", "Trincomalee", "Eastern", setOf(BEACH, HERITAGE), 8.5874, 81.2152, EAST,
            "Deep-water harbour, temples and calm east-coast beaches",
            listOf("Koneswaram Temple on Swami Rock", "Snorkelling at Pigeon Island", "Nilaveli Beach")),
        Destination("arugam-bay", "Arugam Bay", "Arugam Bay", "Eastern", setOf(BEACH, ADVENTURE), 6.8400, 81.8360, (4..10).toSet(),
            "Sri Lanka's surf capital in the east-coast season",
            listOf("The Main Point surf break", "Safari at nearby Kumana National Park", "Elephant Rock and Pottuvil Point")),
        Destination("pasikudah", "Pasikudah", "Pasikudah", "Eastern", setOf(BEACH), 7.9269, 81.5586, EAST,
            "A long, shallow, calm bay that's perfect for swimming",
            listOf("Wade far out in waist-deep water", "Kalkudah beach next door", "Day trip to Batticaloa lagoon")),
        Destination("jaffna", "Jaffna", "Jaffna", "Northern", setOf(HERITAGE, CITY), 9.6615, 80.0255, DRY_ZONE,
            "Tamil culture, temples and the islands of the far north",
            listOf("Nallur Kandaswamy Kovil", "Jaffna Fort", "Casuarina Beach on Karaitivu island", "Jaffna crab curry")),
        Destination("colombo", "Colombo", "Colombo", "Western", setOf(CITY), 6.9271, 79.8612, SOUTH_WEST,
            "The busy commercial capital by the sea",
            listOf("Sunset on Galle Face Green", "Gangaramaya Temple", "Markets and street food in Pettah")),
        Destination("kalpitiya", "Kalpitiya", "Kalpitiya", "North Western", setOf(BEACH, ADVENTURE, WILDLIFE), 8.2333, 79.7667, (5..10).toSet(),
            "Kitesurfing lagoon on a quiet peninsula",
            listOf("Kitesurfing on the lagoon (windy season)", "Dolphin watching (around December to March)", "Unspoilt beaches")),
        Destination("sinharaja", "Sinharaja", "Sinharaja Forest Reserve", "Sabaragamuwa", setOf(WILDLIFE, ADVENTURE), 6.4000, 80.5000, setOf(1, 2, 3, 4),
            "A UNESCO rainforest full of endemic birds",
            listOf("Guided rainforest walks", "Endemic birds in mixed flocks", "Waterfalls and leeches: wear socks!")),
    )

    fun byId(id: String): Destination? = ALL.firstOrNull { it.id == id }

    val provinces: List<String> get() = ALL.map { it.province }.distinct().sorted()
}
