package de.bildgestalter

// Beschreibungen für TalkBack. Jede Zeile hat genau 30 Felder:
// 0 Zurück, 1 Bilder hinzufügen, 2 Löschen, 3 Größer, 4 Kleiner, 5 Nach links drehen,
// 6 Nach rechts drehen, 7 Nach oben verschieben, 8 Nach unten verschieben,
// 9 Nach links verschieben, 10 Nach rechts verschieben, 11 Dicke, 12 Format, 13 Raster,
// 14 Vertikal, 15 Horizontal, 16 bis 29 Farben: Schwarz, Weiß, Rot, Orange, Gelb, Grün,
// Hellblau, Blau, Lila, Rosa, Grau, Braun, Gold, Silber

const val A_BACK = 0
const val A_ADD = 1
const val A_DELETE = 2
const val A_BIGGER = 3
const val A_SMALLER = 4
const val A_ROT_L = 5
const val A_ROT_R = 6
const val A_UP = 7
const val A_DOWN = 8
const val A_LEFT = 9
const val A_RIGHT = 10
const val A_THICK = 11
const val A_FORMAT = 12
const val A_LAYOUT = 13
const val A_VERT = 14
const val A_HORIZ = 15
const val A_COLOR0 = 16
const val A_COUNT = 30

// Sprachen, die dieselben Wörter wie eine andere Zeile nutzen
private val ALIAS = mapOf("iw" to "he", "ji" to "yi", "ary" to "ar", "arz" to "ar")

private val parsed = HashMap<String, List<String>>()

private fun rowOf(key: String): List<String> = synchronized(parsed) {
    parsed.getOrPut(key) {
        val en = A11Y.getValue("en").split("|")
        val p = (A11Y[key] ?: A11Y.getValue("en")).split("|")
        List(A_COUNT) { i -> p.getOrElse(i) { "" }.ifBlank { en.getOrElse(i) { "" } } }
    }
}

/** Beschreibungstext für TalkBack in der aktuellen Sprache (fehlende Felder: Englisch). */
fun ta(i: Int): String {
    val c = Lang.code
    return rowOf(ALIAS[c] ?: c)[i]
}

val A11Y: Map<String, String> = mapOf(
    "en" to "Back|Add images|Delete|Larger|Smaller|Rotate left|Rotate right|Move up|Move down|Move left|Move right|Thickness|Format|Layout|Vertical|Horizontal|Black|White|Red|Orange|Yellow|Green|Light blue|Blue|Purple|Pink|Gray|Brown|Gold|Silver",
    "de" to "Zurück|Bilder hinzufügen|Löschen|Größer|Kleiner|Nach links drehen|Nach rechts drehen|Nach oben verschieben|Nach unten verschieben|Nach links verschieben|Nach rechts verschieben|Dicke|Format|Raster|Vertikal|Horizontal|Schwarz|Weiß|Rot|Orange|Gelb|Grün|Hellblau|Blau|Lila|Rosa|Grau|Braun|Gold|Silber",
    "zh" to "返回|添加图片|删除|放大|缩小|向左旋转|向右旋转|向上移动|向下移动|向左移动|向右移动|粗细|格式|布局|竖向|横向|黑色|白色|红色|橙色|黄色|绿色|浅蓝色|蓝色|紫色|粉色|灰色|棕色|金色|银色",
    "zh-Hant" to "返回|新增圖片|刪除|放大|縮小|向左旋轉|向右旋轉|向上移動|向下移動|向左移動|向右移動|粗細|格式|版面|直向|橫向|黑色|白色|紅色|橙色|黃色|綠色|淺藍色|藍色|紫色|粉紅色|灰色|棕色|金色|銀色",
    "hi" to "वापस|चित्र जोड़ें|हटाएँ|बड़ा करें|छोटा करें|बाएँ घुमाएँ|दाएँ घुमाएँ|ऊपर ले जाएँ|नीचे ले जाएँ|बाएँ ले जाएँ|दाएँ ले जाएँ|मोटाई|फ़ॉर्मेट|लेआउट|खड़ा|आड़ा|काला|सफ़ेद|लाल|नारंगी|पीला|हरा|हल्का नीला|नीला|बैंगनी|गुलाबी|स्लेटी|भूरा|सुनहरा|चाँदी जैसा",
    "es" to "Atrás|Añadir imágenes|Eliminar|Más grande|Más pequeño|Girar a la izquierda|Girar a la derecha|Mover hacia arriba|Mover hacia abajo|Mover a la izquierda|Mover a la derecha|Grosor|Formato|Diseño|Vertical|Horizontal|Negro|Blanco|Rojo|Naranja|Amarillo|Verde|Azul claro|Azul|Morado|Rosa|Gris|Marrón|Dorado|Plateado",
    "fr" to "Retour|Ajouter des images|Supprimer|Plus grand|Plus petit|Pivoter à gauche|Pivoter à droite|Déplacer vers le haut|Déplacer vers le bas|Déplacer vers la gauche|Déplacer vers la droite|Épaisseur|Format|Disposition|Vertical|Horizontal|Noir|Blanc|Rouge|Orange|Jaune|Vert|Bleu clair|Bleu|Violet|Rose|Gris|Marron|Or|Argent",
    "ar" to "رجوع|إضافة صور|حذف|أكبر|أصغر|تدوير لليسار|تدوير لليمين|تحريك للأعلى|تحريك للأسفل|تحريك لليسار|تحريك لليمين|السماكة|التنسيق|التخطيط|عمودي|أفقي|أسود|أبيض|أحمر|برتقالي|أصفر|أخضر|أزرق فاتح|أزرق|بنفسجي|وردي|رمادي|بني|ذهبي|فضي",
    "bn" to "পেছনে|ছবি যোগ করুন|মুছুন|বড় করুন|ছোট করুন|বামে ঘোরান|ডানে ঘোরান|উপরে সরান|নিচে সরান|বামে সরান|ডানে সরান|পুরুত্ব|ফরম্যাট|বিন্যাস|উল্লম্ব|আনুভূমিক|কালো|সাদা|লাল|কমলা|হলুদ|সবুজ|হালকা নীল|নীল|বেগুনি|গোলাপি|ধূসর|বাদামি|সোনালি|রুপালি",
    "pt" to "Voltar|Adicionar imagens|Excluir|Maior|Menor|Girar para a esquerda|Girar para a direita|Mover para cima|Mover para baixo|Mover para a esquerda|Mover para a direita|Espessura|Formato|Layout|Vertical|Horizontal|Preto|Branco|Vermelho|Laranja|Amarelo|Verde|Azul-claro|Azul|Roxo|Rosa|Cinza|Marrom|Dourado|Prateado",
    "ru" to "Назад|Добавить изображения|Удалить|Больше|Меньше|Повернуть влево|Повернуть вправо|Переместить вверх|Переместить вниз|Переместить влево|Переместить вправо|Толщина|Формат|Макет|Вертикально|Горизонтально|Чёрный|Белый|Красный|Оранжевый|Жёлтый|Зелёный|Голубой|Синий|Фиолетовый|Розовый|Серый|Коричневый|Золотой|Серебряный",
    "ur" to "واپس|تصاویر شامل کریں|حذف کریں|بڑا کریں|چھوٹا کریں|بائیں گھمائیں|دائیں گھمائیں|اوپر لے جائیں|نیچے لے جائیں|بائیں لے جائیں|دائیں لے جائیں|موٹائی|فارمیٹ|ترتیب|عمودی|افقی|کالا|سفید|سرخ|نارنجی|پیلا|سبز|ہلکا نیلا|نیلا|جامنی|گلابی|سرمئی|بھورا|سنہری|چاندی جیسا",
    "id" to "Kembali|Tambah gambar|Hapus|Perbesar|Perkecil|Putar ke kiri|Putar ke kanan|Pindah ke atas|Pindah ke bawah|Pindah ke kiri|Pindah ke kanan|Ketebalan|Format|Tata letak|Vertikal|Horizontal|Hitam|Putih|Merah|Oranye|Kuning|Hijau|Biru muda|Biru|Ungu|Merah muda|Abu-abu|Cokelat|Emas|Perak",
    "ja" to "戻る|画像を追加|削除|大きくする|小さくする|左に回転|右に回転|上に移動|下に移動|左に移動|右に移動|太さ|フォーマット|レイアウト|縦|横|黒|白|赤|オレンジ|黄|緑|水色|青|紫|ピンク|グレー|茶|金|銀",
    "pcm" to "Go back|Add pictures|Delete|Make am big|Make am small|Turn go left|Turn go right|Move go up|Move go down|Move go left|Move go right|How thick|Format|Layout|Up and down|Side to side|Black|White|Red|Orange|Yellow|Green|Light blue|Blue|Purple|Pink|Grey|Brown|Gold|Silver",
    "mr" to "मागे|चित्रे जोडा|हटवा|मोठे करा|लहान करा|डावीकडे फिरवा|उजवीकडे फिरवा|वर हलवा|खाली हलवा|डावीकडे हलवा|उजवीकडे हलवा|जाडी|स्वरूप|मांडणी|उभे|आडवे|काळा|पांढरा|लाल|नारिंगी|पिवळा|हिरवा|फिकट निळा|निळा|जांभळा|गुलाबी|राखाडी|तपकिरी|सोनेरी|चंदेरी",
    "te" to "వెనుకకు|చిత్రాలను జోడించండి|తొలగించండి|పెద్దది చేయండి|చిన్నది చేయండి|ఎడమకు తిప్పండి|కుడికి తిప్పండి|పైకి జరపండి|కిందకు జరపండి|ఎడమకు జరపండి|కుడికి జరపండి|మందం|ఫార్మాట్|లేఅవుట్|నిలువు|అడ్డం|నలుపు|తెలుపు|ఎరుపు|నారింజ|పసుపు|ఆకుపచ్చ|లేత నీలం|నీలం|ఊదా|గులాబీ|బూడిద|గోధుమ|బంగారు|వెండి",
    "tr" to "Geri|Görsel ekle|Sil|Büyüt|Küçült|Sola döndür|Sağa döndür|Yukarı taşı|Aşağı taşı|Sola taşı|Sağa taşı|Kalınlık|Biçim|Düzen|Dikey|Yatay|Siyah|Beyaz|Kırmızı|Turuncu|Sarı|Yeşil|Açık mavi|Mavi|Mor|Pembe|Gri|Kahverengi|Altın|Gümüş",
    "ta" to "பின்செல்|படங்களைச் சேர்|நீக்கு|பெரிதாக்கு|சிறிதாக்கு|இடப்பக்கம் சுழற்று|வலப்பக்கம் சுழற்று|மேலே நகர்த்து|கீழே நகர்த்து|இடப்பக்கம் நகர்த்து|வலப்பக்கம் நகர்த்து|தடிமன்|வடிவம்|அமைப்பு|செங்குத்து|கிடைமட்டம்|கருப்பு|வெள்ளை|சிவப்பு|ஆரஞ்சு|மஞ்சள்|பச்சை|வெளிர் நீலம்|நீலம்|ஊதா|இளஞ்சிவப்பு|சாம்பல்|பழுப்பு|தங்கம்|வெள்ளி",
    "yue" to "返回|加入圖片|刪除|放大|縮細|向左轉|向右轉|向上移|向下移|向左移|向右移|粗幼|格式|版面|直向|橫向|黑色|白色|紅色|橙色|黃色|綠色|淺藍色|藍色|紫色|粉紅色|灰色|啡色|金色|銀色",
    "vi" to "Quay lại|Thêm hình ảnh|Xóa|Lớn hơn|Nhỏ hơn|Xoay trái|Xoay phải|Di chuyển lên|Di chuyển xuống|Di chuyển sang trái|Di chuyển sang phải|Độ dày|Định dạng|Bố cục|Dọc|Ngang|Đen|Trắng|Đỏ|Cam|Vàng|Xanh lá|Xanh nhạt|Xanh dương|Tím|Hồng|Xám|Nâu|Vàng kim|Bạc",
    "fil" to "Bumalik|Magdagdag ng mga larawan|Burahin|Palakihin|Paliitin|I-ikot pakaliwa|I-ikot pakanan|Ilipat pataas|Ilipat pababa|Ilipat pakaliwa|Ilipat pakanan|Kapal|Format|Layout|Patayo|Pahiga|Itim|Puti|Pula|Kahel|Dilaw|Berde|Maputlang asul|Asul|Lila|Rosas|Abo|Kayumanggi|Ginto|Pilak",
        "ko" to "뒤로|이미지 추가|삭제|크게|작게|왼쪽으로 회전|오른쪽으로 회전|위로 이동|아래로 이동|왼쪽으로 이동|오른쪽으로 이동|두께|형식|레이아웃|세로|가로|검정|흰색|빨강|주황|노랑|초록|하늘색|파랑|보라|분홍|회색|갈색|금색|은색",
    "it" to "Indietro|Aggiungi immagini|Elimina|Più grande|Più piccolo|Ruota a sinistra|Ruota a destra|Sposta in alto|Sposta in basso|Sposta a sinistra|Sposta a destra|Spessore|Formato|Layout|Verticale|Orizzontale|Nero|Bianco|Rosso|Arancione|Giallo|Verde|Azzurro|Blu|Viola|Rosa|Grigio|Marrone|Oro|Argento",
    "pl" to "Wstecz|Dodaj obrazy|Usuń|Większy|Mniejszy|Obróć w lewo|Obróć w prawo|Przesuń w górę|Przesuń w dół|Przesuń w lewo|Przesuń w prawo|Grubość|Format|Układ|Pionowo|Poziomo|Czarny|Biały|Czerwony|Pomarańczowy|Żółty|Zielony|Jasnoniebieski|Niebieski|Fioletowy|Różowy|Szary|Brązowy|Złoty|Srebrny",
    "uk" to "Назад|Додати зображення|Видалити|Більше|Менше|Повернути ліворуч|Повернути праворуч|Перемістити вгору|Перемістити вниз|Перемістити ліворуч|Перемістити праворуч|Товщина|Формат|Макет|Вертикально|Горизонтально|Чорний|Білий|Червоний|Помаранчевий|Жовтий|Зелений|Блакитний|Синій|Фіолетовий|Рожевий|Сірий|Коричневий|Золотий|Срібний",
    "nl" to "Terug|Afbeeldingen toevoegen|Verwijderen|Groter|Kleiner|Naar links draaien|Naar rechts draaien|Omhoog verplaatsen|Omlaag verplaatsen|Naar links verplaatsen|Naar rechts verplaatsen|Dikte|Formaat|Indeling|Verticaal|Horizontaal|Zwart|Wit|Rood|Oranje|Geel|Groen|Lichtblauw|Blauw|Paars|Roze|Grijs|Bruin|Goud|Zilver",
    "fa" to "بازگشت|افزودن تصاویر|حذف|بزرگ‌تر|کوچک‌تر|چرخش به چپ|چرخش به راست|حرکت به بالا|حرکت به پایین|حرکت به چپ|حرکت به راست|ضخامت|قالب|چیدمان|عمودی|افقی|سیاه|سفید|قرمز|نارنجی|زرد|سبز|آبی روشن|آبی|بنفش|صورتی|خاکستری|قهوه‌ای|طلایی|نقره‌ای",
    "th" to "ย้อนกลับ|เพิ่มรูปภาพ|ลบ|ใหญ่ขึ้น|เล็กลง|หมุนซ้าย|หมุนขวา|เลื่อนขึ้น|เลื่อนลง|เลื่อนไปทางซ้าย|เลื่อนไปทางขวา|ความหนา|รูปแบบ|เลย์เอาต์|แนวตั้ง|แนวนอน|สีดำ|สีขาว|สีแดง|สีส้ม|สีเหลือง|สีเขียว|สีฟ้า|สีน้ำเงิน|สีม่วง|สีชมพู|สีเทา|สีน้ำตาล|สีทอง|สีเงิน",
    "ro" to "Înapoi|Adaugă imagini|Șterge|Mai mare|Mai mic|Rotește la stânga|Rotește la dreapta|Mută în sus|Mută în jos|Mută la stânga|Mută la dreapta|Grosime|Format|Aspect|Vertical|Orizontal|Negru|Alb|Roșu|Portocaliu|Galben|Verde|Albastru deschis|Albastru|Mov|Roz|Gri|Maro|Auriu|Argintiu",
    "cs" to "Zpět|Přidat obrázky|Smazat|Větší|Menší|Otočit doleva|Otočit doprava|Posunout nahoru|Posunout dolů|Posunout doleva|Posunout doprava|Tloušťka|Formát|Rozvržení|Svisle|Vodorovně|Černá|Bílá|Červená|Oranžová|Žlutá|Zelená|Světle modrá|Modrá|Fialová|Růžová|Šedá|Hnědá|Zlatá|Stříbrná",
    "hu" to "Vissza|Képek hozzáadása|Törlés|Nagyobb|Kisebb|Forgatás balra|Forgatás jobbra|Mozgatás felfelé|Mozgatás lefelé|Mozgatás balra|Mozgatás jobbra|Vastagság|Formátum|Elrendezés|Függőleges|Vízszintes|Fekete|Fehér|Piros|Narancssárga|Sárga|Zöld|Világoskék|Kék|Lila|Rózsaszín|Szürke|Barna|Arany|Ezüst",
    "el" to "Πίσω|Προσθήκη εικόνων|Διαγραφή|Μεγαλύτερο|Μικρότερο|Περιστροφή αριστερά|Περιστροφή δεξιά|Μετακίνηση πάνω|Μετακίνηση κάτω|Μετακίνηση αριστερά|Μετακίνηση δεξιά|Πάχος|Μορφή|Διάταξη|Κάθετα|Οριζόντια|Μαύρο|Λευκό|Κόκκινο|Πορτοκαλί|Κίτρινο|Πράσινο|Γαλάζιο|Μπλε|Μοβ|Ροζ|Γκρι|Καφέ|Χρυσό|Ασημί",
    "sv" to "Tillbaka|Lägg till bilder|Ta bort|Större|Mindre|Rotera åt vänster|Rotera åt höger|Flytta uppåt|Flytta nedåt|Flytta åt vänster|Flytta åt höger|Tjocklek|Format|Layout|Lodrät|Vågrät|Svart|Vit|Röd|Orange|Gul|Grön|Ljusblå|Blå|Lila|Rosa|Grå|Brun|Guld|Silver",
    "no" to "Tilbake|Legg til bilder|Slett|Større|Mindre|Roter til venstre|Roter til høyre|Flytt opp|Flytt ned|Flytt til venstre|Flytt til høyre|Tykkelse|Format|Layout|Loddrett|Vannrett|Svart|Hvit|Rød|Oransje|Gul|Grønn|Lyseblå|Blå|Lilla|Rosa|Grå|Brun|Gull|Sølv",
    "da" to "Tilbage|Tilføj billeder|Slet|Større|Mindre|Drej til venstre|Drej til højre|Flyt op|Flyt ned|Flyt til venstre|Flyt til højre|Tykkelse|Format|Layout|Lodret|Vandret|Sort|Hvid|Rød|Orange|Gul|Grøn|Lyseblå|Blå|Lilla|Lyserød|Grå|Brun|Guld|Sølv",
    "fi" to "Takaisin|Lisää kuvia|Poista|Suurempi|Pienempi|Kierrä vasemmalle|Kierrä oikealle|Siirrä ylös|Siirrä alas|Siirrä vasemmalle|Siirrä oikealle|Paksuus|Muoto|Asettelu|Pysty|Vaaka|Musta|Valkoinen|Punainen|Oranssi|Keltainen|Vihreä|Vaaleansininen|Sininen|Violetti|Vaaleanpunainen|Harmaa|Ruskea|Kulta|Hopea",
    "bg" to "Назад|Добавяне на изображения|Изтриване|По-голямо|По-малко|Завъртане наляво|Завъртане надясно|Преместване нагоре|Преместване надолу|Преместване наляво|Преместване надясно|Дебелина|Формат|Оформление|Вертикално|Хоризонтално|Черно|Бяло|Червено|Оранжево|Жълто|Зелено|Светлосиньо|Синьо|Лилаво|Розово|Сиво|Кафяво|Златно|Сребърно",
    "he" to "חזרה|הוספת תמונות|מחיקה|גדול יותר|קטן יותר|סיבוב שמאלה|סיבוב ימינה|הזזה למעלה|הזזה למטה|הזזה שמאלה|הזזה ימינה|עובי|תבנית|פריסה|אנכי|אופקי|שחור|לבן|אדום|כתום|צהוב|ירוק|תכלת|כחול|סגול|ורוד|אפור|חום|זהב|כסף",
    "sk" to "Späť|Pridať obrázky|Odstrániť|Väčšie|Menšie|Otočiť doľava|Otočiť doprava|Posunúť nahor|Posunúť nadol|Posunúť doľava|Posunúť doprava|Hrúbka|Formát|Rozloženie|Zvisle|Vodorovne|Čierna|Biela|Červená|Oranžová|Žltá|Zelená|Svetlomodrá|Modrá|Fialová|Ružová|Sivá|Hnedá|Zlatá|Strieborná",
    "hr" to "Natrag|Dodaj slike|Izbriši|Veće|Manje|Zakreni ulijevo|Zakreni udesno|Pomakni gore|Pomakni dolje|Pomakni ulijevo|Pomakni udesno|Debljina|Format|Raspored|Okomito|Vodoravno|Crna|Bijela|Crvena|Narančasta|Žuta|Zelena|Svijetloplava|Plava|Ljubičasta|Ružičasta|Siva|Smeđa|Zlatna|Srebrna",
    "sr" to "Назад|Додај слике|Обриши|Веће|Мање|Окрени улево|Окрени удесно|Помери нагоре|Помери надоле|Помери улево|Помери удесно|Дебљина|Формат|Распоред|Вертикално|Хоризонтално|Црна|Бела|Црвена|Наранџаста|Жута|Зелена|Светлоплава|Плава|Љубичаста|Розе|Сива|Браон|Златна|Сребрна",
        "sl" to "Nazaj|Dodaj slike|Izbriši|Večje|Manjše|Zasukaj v levo|Zasukaj v desno|Premakni navzgor|Premakni navzdol|Premakni v levo|Premakni v desno|Debelina|Oblika|Postavitev|Navpično|Vodoravno|Črna|Bela|Rdeča|Oranžna|Rumena|Zelena|Svetlomodra|Modra|Vijolična|Roza|Siva|Rjava|Zlata|Srebrna",
    "lt" to "Atgal|Pridėti vaizdų|Ištrinti|Didesnis|Mažesnis|Pasukti kairėn|Pasukti dešinėn|Perkelti aukštyn|Perkelti žemyn|Perkelti kairėn|Perkelti dešinėn|Storis|Formatas|Maketas|Vertikaliai|Horizontaliai|Juoda|Balta|Raudona|Oranžinė|Geltona|Žalia|Šviesiai mėlyna|Mėlyna|Violetinė|Rožinė|Pilka|Ruda|Auksinė|Sidabrinė",
    "lv" to "Atpakaļ|Pievienot attēlus|Dzēst|Lielāks|Mazāks|Pagriezt pa kreisi|Pagriezt pa labi|Pārvietot uz augšu|Pārvietot uz leju|Pārvietot pa kreisi|Pārvietot pa labi|Biezums|Formāts|Izkārtojums|Vertikāli|Horizontāli|Melna|Balta|Sarkana|Oranža|Dzeltena|Zaļa|Gaiši zila|Zila|Violeta|Rozā|Pelēka|Brūna|Zelta|Sudraba",
    "et" to "Tagasi|Lisa pildid|Kustuta|Suurem|Väiksem|Pööra vasakule|Pööra paremale|Liiguta üles|Liiguta alla|Liiguta vasakule|Liiguta paremale|Paksus|Vorming|Paigutus|Vertikaalne|Horisontaalne|Must|Valge|Punane|Oranž|Kollane|Roheline|Helesinine|Sinine|Lilla|Roosa|Hall|Pruun|Kuldne|Hõbedane",
    "sq" to "Mbrapa|Shto imazhe|Fshi|Më i madh|Më i vogël|Rrotullo majtas|Rrotullo djathtas|Lëviz lart|Lëviz poshtë|Lëviz majtas|Lëviz djathtas|Trashësia|Formati|Paraqitja|Vertikale|Horizontale|E zezë|E bardhë|E kuqe|Portokalli|E verdhë|E gjelbër|Blu e hapur|Blu|Vjollcë|Rozë|Gri|Kafe|Ari|Argjend",
    "ca" to "Enrere|Afegeix imatges|Suprimeix|Més gran|Més petit|Gira a l'esquerra|Gira a la dreta|Mou cap amunt|Mou cap avall|Mou cap a l'esquerra|Mou cap a la dreta|Gruix|Format|Disseny|Vertical|Horitzontal|Negre|Blanc|Vermell|Taronja|Groc|Verd|Blau clar|Blau|Lila|Rosa|Gris|Marró|Or|Plata",
    "af" to "Terug|Voeg beelde by|Skrap|Groter|Kleiner|Draai links|Draai regs|Skuif op|Skuif af|Skuif links|Skuif regs|Dikte|Formaat|Uitleg|Vertikaal|Horisontaal|Swart|Wit|Rooi|Oranje|Geel|Groen|Ligblou|Blou|Pers|Pienk|Grys|Bruin|Goud|Silwer",
    "is" to "Til baka|Bæta við myndum|Eyða|Stærra|Minna|Snúa til vinstri|Snúa til hægri|Færa upp|Færa niður|Færa til vinstri|Færa til hægri|Þykkt|Snið|Útlit|Lóðrétt|Lárétt|Svart|Hvítt|Rautt|Appelsínugult|Gult|Grænt|Ljósblátt|Blátt|Fjólublátt|Bleikt|Grátt|Brúnt|Gull|Silfur",
    "ms" to "Kembali|Tambah imej|Padam|Lebih besar|Lebih kecil|Putar ke kiri|Putar ke kanan|Alih ke atas|Alih ke bawah|Alih ke kiri|Alih ke kanan|Ketebalan|Format|Susun atur|Menegak|Mendatar|Hitam|Putih|Merah|Oren|Kuning|Hijau|Biru muda|Biru|Ungu|Merah jambu|Kelabu|Perang|Emas|Perak",
    "sw" to "Rudi|Ongeza picha|Futa|Kubwa zaidi|Ndogo zaidi|Zungusha kushoto|Zungusha kulia|Sogeza juu|Sogeza chini|Sogeza kushoto|Sogeza kulia|Unene|Umbizo|Mpangilio|Wima|Mlalo|Nyeusi|Nyeupe|Nyekundu|Rangi ya machungwa|Njano|Kijani|Samawati hafifu|Samawati|Zambarau|Waridi|Kijivu|Kahawia|Dhahabu|Fedha",
    "kn" to "ಹಿಂದೆ|ಚಿತ್ರಗಳನ್ನು ಸೇರಿಸಿ|ಅಳಿಸಿ|ದೊಡ್ಡದು ಮಾಡಿ|ಚಿಕ್ಕದು ಮಾಡಿ|ಎಡಕ್ಕೆ ತಿರುಗಿಸಿ|ಬಲಕ್ಕೆ ತಿರುಗಿಸಿ|ಮೇಲಕ್ಕೆ ಸರಿಸಿ|ಕೆಳಕ್ಕೆ ಸರಿಸಿ|ಎಡಕ್ಕೆ ಸರಿಸಿ|ಬಲಕ್ಕೆ ಸರಿಸಿ|ದಪ್ಪ|ಸ್ವರೂಪ|ವಿನ್ಯಾಸ|ಲಂಬ|ಅಡ್ಡ|ಕಪ್ಪು|ಬಿಳಿ|ಕೆಂಪು|ಕಿತ್ತಳೆ|ಹಳದಿ|ಹಸಿರು|ತಿಳಿ ನೀಲಿ|ನೀಲಿ|ನೇರಳೆ|ಗುಲಾಬಿ|ಬೂದು|ಕಂದು|ಚಿನ್ನ|ಬೆಳ್ಳಿ",
    "gu" to "પાછળ|છબીઓ ઉમેરો|કાઢી નાખો|મોટું કરો|નાનું કરો|ડાબે ફેરવો|જમણે ફેરવો|ઉપર ખસેડો|નીચે ખસેડો|ડાબે ખસેડો|જમણે ખસેડો|જાડાઈ|ફોર્મેટ|લેઆઉટ|ઊભું|આડું|કાળો|સફેદ|લાલ|નારંગી|પીળો|લીલો|આછો વાદળી|વાદળી|જાંબલી|ગુલાબી|રાખોડી|ભૂરો|સોનેરી|ચાંદી",
    "ml" to "പിന്നോട്ട്|ചിത്രങ്ങൾ ചേർക്കുക|ഇല്ലാതാക്കുക|വലുതാക്കുക|ചെറുതാക്കുക|ഇടത്തോട്ട് തിരിക്കുക|വലത്തോട്ട് തിരിക്കുക|മുകളിലേക്ക് നീക്കുക|താഴേക്ക് നീക്കുക|ഇടത്തോട്ട് നീക്കുക|വലത്തോട്ട് നീക്കുക|കനം|ഫോർമാറ്റ്|ലേഔട്ട്|ലംബം|തിരശ്ചീനം|കറുപ്പ്|വെള്ള|ചുവപ്പ്|ഓറഞ്ച്|മഞ്ഞ|പച്ച|ഇളം നീല|നീല|ധൂമ്രവർണ്ണം|പിങ്ക്|ചാരനിറം|തവിട്ട്|സ്വർണ്ണം|വെള്ളി",
    "pa" to "ਪਿੱਛੇ|ਤਸਵੀਰਾਂ ਸ਼ਾਮਲ ਕਰੋ|ਮਿਟਾਓ|ਵੱਡਾ ਕਰੋ|ਛੋਟਾ ਕਰੋ|ਖੱਬੇ ਘੁੰਮਾਓ|ਸੱਜੇ ਘੁੰਮਾਓ|ਉੱਪਰ ਲਿਜਾਓ|ਹੇਠਾਂ ਲਿਜਾਓ|ਖੱਬੇ ਲਿਜਾਓ|ਸੱਜੇ ਲਿਜਾਓ|ਮੋਟਾਈ|ਫਾਰਮੈਟ|ਲੇਆਉਟ|ਖੜ੍ਹਵਾਂ|ਲੇਟਵਾਂ|ਕਾਲਾ|ਚਿੱਟਾ|ਲਾਲ|ਸੰਤਰੀ|ਪੀਲਾ|ਹਰਾ|ਹਲਕਾ ਨੀਲਾ|ਨੀਲਾ|ਜਾਮਣੀ|ਗੁਲਾਬੀ|ਸਲੇਟੀ|ਭੂਰਾ|ਸੁਨਹਿਰੀ|ਚਾਂਦੀ ਰੰਗਾ",
    "ne" to "पछाडि|तस्बिरहरू थप्नुहोस्|हटाउनुहोस्|ठूलो बनाउनुहोस्|सानो बनाउनुहोस्|बायाँ घुमाउनुहोस्|दायाँ घुमाउनुहोस्|माथि सार्नुहोस्|तल सार्नुहोस्|बायाँ सार्नुहोस्|दायाँ सार्नुहोस्|मोटाइ|ढाँचा|लेआउट|ठाडो|तेर्सो|कालो|सेतो|रातो|सुन्तला|पहेँलो|हरियो|हल्का निलो|निलो|बैजनी|गुलाबी|धूसर|खैरो|सुनौलो|चाँदी रङ",
    "si" to "ආපසු|රූප එක් කරන්න|මකන්න|විශාල කරන්න|කුඩා කරන්න|වමට කරකවන්න|දකුණට කරකවන්න|ඉහළට ගෙන යන්න|පහළට ගෙන යන්න|වමට ගෙන යන්න|දකුණට ගෙන යන්න|ඝනකම|ආකෘතිය|පිරිසැලසුම|සිරස්|තිරස්|කළු|සුදු|රතු|තැඹිලි|කහ|කොළ|ලා නිල්|නිල්|දම්|රෝස|අළු|දුඹුරු|රන්|රිදී",
    "kk" to "Артқа|Суреттер қосу|Жою|Үлкейту|Кішірейту|Солға бұру|Оңға бұру|Жоғары жылжыту|Төмен жылжыту|Солға жылжыту|Оңға жылжыту|Қалыңдығы|Пішім|Макет|Тік|Көлденең|Қара|Ақ|Қызыл|Қызғылт сары|Сары|Жасыл|Ашық көк|Көк|Күлгін|Қызғылт|Сұр|Қоңыр|Алтын|Күміс",
    "uz" to "Orqaga|Rasmlar qoʻshish|Oʻchirish|Kattaroq|Kichikroq|Chapga burish|Oʻngga burish|Yuqoriga siljitish|Pastga siljitish|Chapga siljitish|Oʻngga siljitish|Qalinligi|Format|Joylashuv|Vertikal|Gorizontal|Qora|Oq|Qizil|Toʻq sariq|Sariq|Yashil|Havorang|Koʻk|Binafsha|Pushti|Kulrang|Jigarrang|Oltin|Kumush",
    "az" to "Geri|Şəkillər əlavə et|Sil|Daha böyük|Daha kiçik|Sola fırlat|Sağa fırlat|Yuxarı apar|Aşağı apar|Sola apar|Sağa apar|Qalınlıq|Format|Düzən|Şaquli|Üfüqi|Qara|Ağ|Qırmızı|Narıncı|Sarı|Yaşıl|Açıq mavi|Mavi|Bənövşəyi|Çəhrayı|Boz|Qəhvəyi|Qızılı|Gümüşü",
    "ka" to "უკან|სურათების დამატება|წაშლა|გაზრდა|შემცირება|მარცხნივ შეტრიალება|მარჯვნივ შეტრიალება|ზევით გადაადგილება|ქვევით გადაადგილება|მარცხნივ გადაადგილება|მარჯვნივ გადაადგილება|სისქე|ფორმატი|განლაგება|ვერტიკალური|ჰორიზონტალური|შავი|თეთრი|წითელი|ნარინჯისფერი|ყვითელი|მწვანე|ცისფერი|ლურჯი|იისფერი|ვარდისფერი|ნაცრისფერი|ყავისფერი|ოქროსფერი|ვერცხლისფერი",
    // ENDE-DER-DATEN
)
