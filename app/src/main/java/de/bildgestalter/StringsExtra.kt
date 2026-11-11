package de.bildgestalter

// Zusätzliche Sprachen. Jede Zeile hat bis zu 35 Felder, getrennt durch "|".
// Leere Felder fallen automatisch auf Englisch zurück.
private val EXTRA: Map<String, String> = mapOf(
    "af" to "Vryskrif|Collage|Ry|Terug na tuisskerm?|Ongeëksporteerde veranderinge sal verlore gaan.|Ja|Kanselleer|Voeg eers beelde of teks by.|Besig om uit te voer|Gestoor in galery (%s)|Uitvoer het misluk. Probeer 'n laer resolusie.|Resolusie|Voer uit|Raam|Teks|Gemiddeld|Spaarsaam|Lettertipe|Lettertipekleur|Agtergrondarea|Teksblokkie-agtergrond|Geen|Ronde hoeke|Belyning|Links|Gesentreer|Regs|Deursigtigheid|Reëlspasiëring|Letterspasiëring|Reëlkromming|Tikmasjien|Handskrif|Vet|Kursief",
    "ak" to "Ankasa|Mfonini nkabom|Nhyehyɛe|San kɔ fie?|Nsakraeɛ a wonnsoma no bɛyera.|Aane|Gyae|Fa mfonini anaa nkyerɛwee ka ho kane.|Resoma|Wɔakora no wɔ gallery mu (%s)|Nsoma no anyɛ yie. Sɔ resolution a ɛte hɔ ketewa hwɛ.||Soma||Nkyerɛwee|Ntam||||||Hwee|||Benkum|Mfinimfini|Nifa",
    "grc" to "Ἐλεύθερον|Σύνθεσις|Στίχος|Ἐπανελθεῖν εἰς τὴν ἀρχήν;|Αἱ μὴ ἐξαχθεῖσαι μεταβολαὶ ἀπολοῦνται.|Ναί|Ἀκύρωσις|Πρῶτον προσθεῖναι εἰκόνας ἢ κείμενον.|Ἐξάγεται|Ἀπεσώθη ἐν τῷ ἀρχείῳ (%s)|Ἡ ἐξαγωγὴ ἀπέτυχεν. Πειρῶ μείονι ἀναλύσει.|Ἀνάλυσις|Ἐξάγειν|Πλαίσιον|Κείμενον|Μέση|Φειδωλή|Τύπος γραμμάτων|Χρῶμα γραμμάτων|Χῶρος ὑποβάθρου|Ὑπόβαθρον κειμένου|Οὐδέν|Καμπύλαι γωνίαι|Στοίχισις|Ἀριστερά|Μέσον|Δεξιά|Διαφάνεια|Διάστημα γραμμῶν|Διάστημα γραμμάτων|Καμπυλότης γραμμῆς|Γραφομηχανή|Χειρόγραφον|Παχύ|Πλάγιον",
    "as" to "ফ্ৰীষ্টাইল|কলাজ|শাৰী|হোমলৈ উভটিব নেকি?|ৰপ্তানি নকৰা সলনিবোৰ নিঃশেষ হ'ব।|হয়|বাতিল|প্ৰথমে ছবি বা লিখনী যোগ কৰক।|ৰপ্তানি হৈ আছে|গেলেৰীত সংৰক্ষণ কৰা হ'ল (%s)|ৰপ্তানি বিফল হ'ল। কম ৰিজলিউচন চেষ্টা কৰক।|ৰিজলিউচন|ৰপ্তানি|ফ্ৰেম|লিখনী|মধ্যম|সাশ্ৰয়ী|ফণ্ট|ফণ্টৰ ৰং|পটভূমিৰ ক্ষেত্ৰ|লিখনী বাকচৰ পটভূমি|একো নাই|গোলাকাৰ চুকবোৰ|সংৰেখণ|বাওঁ|মাজত|সোঁ|স্বচ্ছতা|শাৰীৰ ব্যৱধান|আখৰৰ ব্যৱধান|শাৰীৰ বক্ৰতা|টাইপৰাইটাৰ|হাতৰ লিখা|গাঢ়|ঢলা",
    "bm" to "|||Ka segin so?||Awɔ|Ka bila||||||||Sɛbɛn|||||||Foyi",
    "eu" to "Estilo askea|Collagea|Errenkada|Hasierara itzuli?|Esportatu gabeko aldaketak galduko dira.|Bai|Utzi|Gehitu lehenik irudiak edo testua.|Esportatzen|Galerian gordeta (%s)|Esportazioak huts egin du. Saiatu bereizmen txikiagoarekin.|Bereizmena|Esportatu|Markoa|Testua|Ertaina|Aurrezlea|Letra-tipoa|Letra-kolorea|Atzeko planoko eremua|Testu-koadroaren atzeko planoa|Bat ere ez|Ertz biribilduak|Lerrokatzea|Ezkerrean|Erdian|Eskuinean|Gardentasuna|Lerro-tartea|Letra-tartea|Lerroaren kurbadura|Idazmakina|Eskuz idatzia|Lodia|Etzana",
    "my" to "လွတ်လပ်သော|ကိုလာ့ဂျ်|အတန်း|ပင်မစာမျက်နှာသို့ ပြန်မလား။|ထုတ်ယူမထားသော ပြောင်းလဲမှုများ ပျောက်သွားပါမည်။|ဟုတ်|မလုပ်တော့ပါ|ပထမ ပုံများ သို့မဟုတ် စာသား ထည့်ပါ။|ထုတ်ယူနေသည်|ပြခန်းတွင် သိမ်းပြီး (%s)|ထုတ်ယူမှု မအောင်မြင်ပါ။ ပိုနိမ့်သော ရေဆာလူးရှင်း စမ်းကြည့်ပါ။|ရေဆာလူးရှင်း|ထုတ်ယူရန်|ဘောင်|စာသား|အလယ်အလတ်|ချွေတာ|ဖောင့်|ဖောင့်အရောင်|နောက်ခံနေရာ|စာသားအကွက် နောက်ခံ|မရှိ|ထောင့်ဝိုင်း|ညှိခြင်း|ဘယ်|အလယ်|ညာ|ပွင့်လင်းမြင်နိုင်မှု|စာကြောင်းအကွာ|စာလုံးအကွာ|စာကြောင်းကွေ့|လက်နှိပ်စက်|လက်ရေး|စာလုံးမည်း|စာလုံးစောင်း",
    "ceb" to "Libre|Collage|Laray|Balik sa panimalay?|Mawala ang wala pa ma-export nga mga kausaban.|Oo|Kanselahon|Pagdugang una og mga hulagway o teksto.|Nag-export|Na-save sa galeriya (%s)|Napakyas ang pag-export. Sulayi ang mas ubos nga resolusyon.|Resolusyon|I-export|Frame|Teksto|Tunga-tunga|Barato|Font|Kolor sa font|Dapit sa background|Background sa text box|Walay usa|Lingin nga mga eskina|Pag-align|Wala|Tunga|Tuo|Transparency|Gilay-on sa linya|Gilay-on sa letra|Kurba sa linya|Makinilya|Sinulat-kamot|Bold|Italic",
    "chr" to "|||||ᎥᎥ|||||||||ᎪᏪᎵ"
)

private fun fix(s: String): String {
    val p = s.split("|").toMutableList()
    while (p.size < S_COUNT) p.add("")
    return p.take(S_COUNT).joinToString("|")
}

/** Alle Sprachen (Grundstock aus Strings.kt plus Zusatzsprachen), jede Zeile mit genau 35 Feldern. */
val DATA: Map<String, String> = (BASE_DATA + EXTRA + MORE).mapValues { fix(it.value) }
