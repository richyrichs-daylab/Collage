package de.bildgestalter

// Baut eine Sprachzeile aus genau 35 Texten (7 Zeilen mit je 5 Feldern).
private fun row(vararg f: String): String = f.joinToString("|")

val MORE3: Map<String, String> = mapOf(
    "so" to row(
        "Bilaash", "Isku-dhafka sawirrada", "Safka", "Ku noqo bogga hore?", "Isbeddellada aan la dhoofin way lumi doonaan.",
        "Haa", "Jooji", "Marka hore ku dar sawirro ama qoraal.", "Waa la dhoofinayaa", "Waxaa lagu keydiyay gallery-ga (%s)",
        "Dhoofintu way fashilantay. Isku day xallin hoose.", "Xallinta", "Dhoofi", "Xuduud", "Qoraal",
        "Dhexdhexaad", "Dhaqaale", "Nooca farta", "Midabka farta", "Aagga gadaasha",
        "Gadaasha sanduuqa qoraalka", "Midna", "Geesaha goobaaban", "Isku-toosinta", "Bidix",
        "Dhexe", "Midig", "Hufnaan", "Farqiga safafka", "Farqiga xarfaha",
        "Foorarka safka", "Makiinadda qorista", "Far gacan", "Dhumuc weyn", "Dhabbaysan"
    ),
    "su" to row(
        "Bébas", "Kolase", "Baris", "Balik ka beranda?", "Parobahan anu acan diékspor bakal leungit.",
        "Enya", "Batal", "Tambahkeun heula gambar atawa téks.", "Keur ngékspor", "Disimpen dina galeri (%s)",
        "Éksporna gagal. Cobian résolusi nu leuwih handap.", "Résolusi", "Ékspor", "Pigura", "Téks",
        "Sedeng", "Hémat", "Fon", "Warna fon", "Aréa latar",
        "Latar kotak téks", "Euweuh", "Juru buleud", "Perataan", "Kénca",
        "Tengah", "Katuhu", "Transparansi", "Jarak baris", "Jarak aksara",
        "Lengkungan baris", "Mesin tik", "Tulisan leungeun", "Kandel", "Miring"
    ),
    "tg" to row(
        "Озод", "Коллаж", "Қатор", "Ба саҳифаи асосӣ бозгардем?", "Тағйиротҳои содиронашуда гум мешаванд.",
        "Бале", "Бекор кардан", "Аввал тасвир ё матн илова кунед.", "Содир шуда истодааст", "Дар галерея захира шуд (%s)",
        "Содирот номуваффақ шуд. Возеҳии пасттарро санҷед.", "Возеҳӣ", "Содирот", "Чорчӯба", "Матн",
        "Миёна", "Сарфакорона", "Ҳуруф", "Ранги ҳуруф", "Минтақаи замина",
        "Заминаи қуттии матн", "Не", "Гӯшаҳои мудаввар", "Мувофиқсозӣ", "Чап",
        "Марказ", "Рост", "Шаффофӣ", "Фосилаи сатрҳо", "Фосилаи ҳарфҳо",
        "Каҷии сатр", "Мошини чоп", "Хатти даст", "Ғафс", "Хам"
    ),
    "bo" to row(
        "རང་དབང་།", "སྡེབ་སྒྲིག", "གྲལ་ཐིག", "གཙོ་ངོས་སུ་ཕྱིར་ལོག་གམ།", "ཕྱིར་འདོན་མ་བྱས་པའི་འགྱུར་བ་རྣམས་བརླག་འགྲོ་ངེས།",
        "ཡིན།", "མེད་པར་བཟོ།", "སྔོན་དུ་པར་རིས་ཡང་ན་ཡིག་ཆ་སྣོན་རོགས།", "ཕྱིར་འདོན་བྱེད་བཞིན་པ།", "པར་མཛོད་དུ་ཉར་ཚགས་བྱས་ཡོད། (%s)",
        "ཕྱིར་འདོན་ཕམ་སོང་། གསལ་ཚད་དམའ་བ་ཚོད་ལྟ་བྱེད་རོགས།", "གསལ་ཚད།", "ཕྱིར་འདོན།", "སྒྲོམ་གཞི།", "ཡིག་ཆ།",
        "འབྲིང་།", "གྲོན་ཆུང་།", "ཡིག་གཟུགས།", "ཡིག་གཟུགས་ཀྱི་ཁ་དོག", "རྒྱབ་ལྗོངས་ཁུལ།",
        "ཡིག་སྒྲོམ་གྱི་རྒྱབ་ལྗོངས།", "མེད།", "ཟུར་འཁོར།", "སྙོམས་སྒྲིག", "གཡོན།",
        "དབུས།", "གཡས།", "དྭངས་གསལ།", "ཐིག་བར།", "ཡིག་འབྲུ་བར།",
        "ཐིག་གི་འཁྱོག་ཚད།", "ཡིག་འཁོར།", "ལག་བྲིས།", "སྦོམ།", "ཕྱོགས།"
    ),
    "ti" to row(
        "ነጻ", "ኮላጅ", "ተርታ", "ናብ መእተዊ ተመለስ?", "ዘይተላእኩ ለውጥታት ክጠፍኡ እዮም።",
        "እወ", "ሰርዝ", "መጀመርታ ስእልታት ወይ ጽሑፍ ወስኽ።", "ይልእኽ ኣሎ", "ኣብ ጋለሪ ተቐሚጡ (%s)",
        "ምልኣኽ ኣይተዓወተን። ትሑት ጥርትዓት ፈትን።", "ጥርትዓት", "ላእኽ", "ፍሬም", "ጽሑፍ",
        "ማእከላይ", "ቆጣቢ", "ፊደል", "ሕብሪ ፊደል", "ናይ ድሕረ-ባይታ ቦታ",
        "ድሕረ-ባይታ ሳጹን ጽሑፍ", "ዋላ ሓደ", "ክቢ ኩርናዓት", "ምምዕርራይ", "ጸጋም",
        "ማእከል", "የማን", "ግሉጽነት", "ርሕቐት መስመር", "ርሕቐት ፊደል",
        "ዕምበባ መስመር", "መጽሓፊ መኪና", "ብኢድ ዝተጻሕፈ", "ድፍረት", "ሰያፍ"
    ),
    "to" to row(
        "Tau", "Fakataha'i fakatātā", "Laine", "Foki ki he ʻapi?", "ʻE mole ʻa e ngaahi liliu naʻe ʻikai tuku atu.",
        "ʻIo", "Fakangata", "Tānaki ʻuluaki ha ngaahi fakatātā pe tohi.", "ʻOku tuku atu", "Kuo tauhi ʻi he fale fakatātā (%s)",
        "Naʻe ʻikai ke lava ʻa e tuku atu. Feinga ki ha fakamaʻamaʻa ʻoku māʻulalo.", "Fakamaʻamaʻa", "Tuku atu", "Tuʻunga", "Tohi",
        "Vahaʻa", "Fakamoʻui", "Fonitā", "Lanu ʻo e fonitā", "Feituʻu ki mui",
        "Ki mui ʻo e puha tohi", "Ngata ʻaki", "Tuliki fakatautau", "Fakahoko tatau", "Toʻohema",
        "Loto", "Toʻomataʻu", "Maʻa", "Vaʻa ʻo e laine", "Vaʻa ʻo e mataʻitohi",
        "Tuliki ʻo e laine", "Mīsini tohi", "Tohi ʻaki e nima", "Matolu", "Fakaheka"
    ),
    "tk" to row(
        "Erkin", "Kolaž", "Hatar", "Baş sahypa dolanmalymy?", "Eksport edilmedik üýtgeşmeler ýitiriler.",
        "Hawa", "Ýatyr", "Ilki bilen suratlary ýa-da tekst goşuň.", "Eksport edilýär", "Galereýada saklandy (%s)",
        "Eksport şowsuz boldy. Pes çözgüdi synap görüň.", "Çözgüt", "Eksport", "Çarçuwa", "Tekst",
        "Orta", "Tygşytly", "Şrift", "Şrift reňki", "Fon meýdany",
        "Tekst gutusynyň fony", "Ýok", "Togalak burçlar", "Deňleşdirmek", "Çep",
        "Merkez", "Sag", "Aýdyňlyk", "Hatar aralygy", "Harp aralygy",
        "Hatar egriligi", "Ýazuw maşyny", "El ýazgysy", "Galyň", "Ýapgyt"
    ),
    "wo" to row(
        "Libre", "Kolaaj", "Rëdd", "Dellusi ci kër gi?", "Soppi yi ñu manul a yóbbu dinañu ñàkk.",
        "Waaw", "Bàyyi", "Yokkal nataal yi walla bind bi njëkk.", "Mu ngi yóbbu", "Denc nañu ko ci galeri bi (%s)",
        "Yóbbu bi amul ndam. Jéem benn dëgg-dëgg bu ndaw.", "Dëgg-dëgg", "Yóbbu", "Kadar", "Bindu",
        "Digg", "Wàññi", "Mbind", "Melo mbind", "Palaas bi ci gannaaw",
        "Gannaawu këyitu bindu", "Amul", "Poñ yu wërsëg", "Tëralin", "Càmmoñ",
        "Digg", "Ndayjoor", "Leer", "Tolluwaay ci diggante rëdd yi", "Tolluwaay ci diggante araf yi",
        "Wërsëg rëdd bi", "Mbind-mbindukaay", "Bind ci loxo", "Rëy", "Daaw"
    ),
    "xh" to row(
        "Isimbo simahla", "Ikholaji", "Umqolo", "Buyela ekhaya?", "Utshintsho olungathunyelwanga luza kulahleka.",
        "Ewe", "Rhoxisa", "Qala ngokongeza imifanekiso okanye isicatshulwa.", "Iyathunyelwa", "Igcinwe kwigalari (%s)",
        "Ukuthumela akuphumelelanga. Zama isisombululo esiphantsi.", "Isisombululo", "Thumela", "Ifreyimu", "Isicatshulwa",
        "Embindini", "Ukonga", "Uhlobo lweleta", "Umbala weleta", "Indawo engasemva",
        "Umva webhokisi yombhalo", "Akukho", "Iikona ezijikelezayo", "Ulungelelaniso", "Ekhohlo",
        "Embindini", "Ekunene", "Ukucaca", "Isithuba somgca", "Isithuba seleta",
        "Ukugoba komgca", "Umatshini wokuchwetheza", "Ukubhala ngesandla", "Eqinileyo", "Egobileyo"
    ),
    "zu" to row(
        "Okukhululekile", "Ikholaji", "Umugqa", "Buyela ekhaya?", "Izinguquko ezingathunyelwanga ziyolahleka.",
        "Yebo", "Khansela", "Qala ngokufaka izithombe noma umbhalo.", "Iyathunyelwa", "Kulondolozwe kugalari (%s)",
        "Ukuthumela kuhlulekile. Zama ukucaca okuphansi.", "Ukucaca kwesithombe", "Thumela", "Uhlaka", "Umbhalo",
        "Maphakathi", "Ukonga", "Uhlobo lohlamvu", "Umbala wohlamvu", "Indawo engemuva",
        "Ingemuva yebhokisi lombhalo", "Lutho", "Amakhona ayindingilizi", "Ukuqondanisa", "Kwesokunxele",
        "Maphakathi", "Kwesokudla", "Ukusobala", "Isikhala phakathi kwemigqa", "Isikhala phakathi kwezinhlamvu",
        "Ukugoba komugqa", "Umshini wokuthayipha", "Ukubhala ngesandla", "Okugqamile", "Okuthambekile"
    )
)
