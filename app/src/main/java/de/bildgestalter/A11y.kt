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
    "hy" to "Հետ|Ավելացնել պատկերներ|Ջնջել|Մեծացնել|Փոքրացնել|Պտտել ձախ|Պտտել աջ|Տեղափոխել վերև|Տեղափոխել ներքև|Տեղափոխել ձախ|Տեղափոխել աջ|Հաստություն|Ձևաչափ|Դասավորություն|Ուղղահայաց|Հորիզոնական|Սև|Սպիտակ|Կարմիր|Նարնջագույն|Դեղին|Կանաչ|Երկնագույն|Կապույտ|Մանուշակագույն|Վարդագույն|Մոխրագույն|Շագանակագույն|Ոսկեգույն|Արծաթագույն",
    "am" to "ተመለስ|ምስሎችን ጨምር|ሰርዝ|ትልቅ አድርግ|ትንሽ አድርግ|ወደ ግራ አሽከርክር|ወደ ቀኝ አሽከርክር|ወደ ላይ ውሰድ|ወደ ታች ውሰድ|ወደ ግራ ውሰድ|ወደ ቀኝ ውሰድ|ውፍረት|ቅርጸት|አቀማመጥ|ቁመታዊ|አግድም|ጥቁር|ነጭ|ቀይ|ብርቱካናማ|ቢጫ|አረንጓዴ|ቀላል ሰማያዊ|ሰማያዊ|ሐምራዊ|ሮዝ|ግራጫ|ቡናማ|ወርቃማ|ብርማ",
    "jv" to "Bali|Tambah gambar|Busak|Gedhekna|Cilikna|Puter ngiwa|Puter nengen|Pindhah munggah|Pindhah mudhun|Pindhah ngiwa|Pindhah nengen|Kandel|Format|Tata letak|Vertikal|Horisontal|Ireng|Putih|Abang|Oranye|Kuning|Ijo|Biru enom|Biru|Ungu|Jambon|Abu-abu|Coklat|Emas|Perak",
    "ha" to "Koma baya|Ƙara hotuna|Share|Ƙara girma|Rage girma|Juya hagu|Juya dama|Matsa sama|Matsa ƙasa|Matsa hagu|Matsa dama|Kauri|Tsari|Shimfiɗa|A tsaye|A kwance|Baƙi|Fari|Ja|Lemu|Rawaya|Kore|Shuɗi mai haske|Shuɗi|Shunayya|Hoda|Toka|Ruwan ƙasa|Zinariya|Azurfa",
    "yo" to "Padà sẹ́yìn|Fi àwòrán kún|Parẹ́|Tóbi síi|Kéré síi|Yí sí òsì|Yí sí ọ̀tún|Gbé sókè|Gbé sísàlẹ̀|Gbé sí òsì|Gbé sí ọ̀tún|Ìnípọn|Ìṣètò|Ìgbékalẹ̀|Ní ìdúró|Ní ìbùsùn|Dúdú|Funfun|Pupa|Ọsàn|Ofeefee|Àwọ̀ ewé|Búlúù tó fẹ́ẹ́rẹ́|Búlúù|Elése àlùkò|Pọ́ńkì|Àwọ̀ eérú|Àwọ̀ ilẹ̀|Wúrà|Fàdákà",
    "ig" to "Laghachi|Tinye foto|Hichapụ|Mee ka ọ dị ukwuu|Mee ka ọ dị nta|Gbaa gburugburu n'aka ekpe|Gbaa gburugburu n'aka nri|Buga elu|Buga ala|Buga n'aka ekpe|Buga n'aka nri|Ọkpụrụkpụ|Nhazi|Ọdịdị|Kwụ ọtọ|Dina ọtọ|Oji|Ọcha|Uhie|Oroma|Odo|Akwụkwọ ndụ|Anụnụ anụnụ dị nro|Anụnụ anụnụ|Pọpụl|Pink|Ntụ|Aja aja|Ọla edo|Ọlaọcha",
    "ku" to "Vegere|Wêne zêde bike|Jê bibe|Mezintir|Biçûktir|Bizivirîne çepê|Bizivirîne rastê|Bibe jor|Bibe jêr|Bibe çepê|Bibe rastê|Stûrahî|Format|Rêzkirin|Vertîkal|Horîzontal|Reş|Spî|Sor|Porteqalî|Zer|Kesk|Şîna vekirî|Şîn|Mor|Pembe|Gewr|Qehweyî|Zêrîn|Zîvîn",
    "ckb" to "گەڕانەوە|زیادکردنی وێنەکان|سڕینەوە|گەورەتر|بچووکتر|ڕاچەرخاندن بۆ چەپ|ڕاچەرخاندن بۆ ڕاست|گواستنەوە بۆ سەرەوە|گواستنەوە بۆ خوارەوە|گواستنەوە بۆ چەپ|گواستنەوە بۆ ڕاست|ئەستووری|شێواز|ڕێکخستن|ستوونی|ئاسۆیی|ڕەش|سپی|سوور|پرتەقاڵی|زەرد|سەوز|شینی کاڵ|شین|مۆر|پەمەیی|خۆڵەمێشی|قاوەیی|زێڕین|زیوین",
    "my" to "နောက်သို့|ပုံများ ထည့်ရန်|ဖျက်ရန်|ပိုကြီးရန်|ပိုသေးရန်|ဘယ်သို့ လှည့်ရန်|ညာသို့ လှည့်ရန်|အပေါ်သို့ရွှေ့ရန်|အောက်သို့ရွှေ့ရန်|ဘယ်သို့ရွှေ့ရန်|ညာသို့ရွှေ့ရန်|အထူ|ဖော်မတ်|အပြင်အဆင်|ဒေါင်လိုက်|အလျားလိုက်|အနက်ရောင်|အဖြူရောင်|အနီရောင်|လိမ္မော်ရောင်|အဝါရောင်|အစိမ်းရောင်|အပြာဖျော့ရောင်|အပြာရောင်|ခရမ်းရောင်|ပန်းရောင်|မီးခိုးရောင်|အညိုရောင်|ရွှေရောင်|ငွေရောင်",
    "km" to "ថយក្រោយ|បន្ថែមរូបភាព|លុប|ធំជាងមុន|តូចជាងមុន|បង្វិលទៅឆ្វេង|បង្វិលទៅស្ដាំ|ផ្លាស់ទីឡើងលើ|ផ្លាស់ទីចុះក្រោម|ផ្លាស់ទីទៅឆ្វេង|ផ្លាស់ទីទៅស្ដាំ|កម្រាស់|ទ្រង់ទ្រាយ|ប្លង់|បញ្ឈរ|ផ្ដេក|ខ្មៅ|ស|ក្រហម|ទឹកក្រូច|លឿង|បៃតង|ខៀវស្រាល|ខៀវ|ស្វាយ|ផ្កាឈូក|ប្រផេះ|ត្នោត|មាស|ប្រាក់",
    "lo" to "ກັບຄືນ|ເພີ່ມຮູບພາບ|ລຶບ|ໃຫຍ່ຂຶ້ນ|ນ້ອຍລົງ|ໝຸນຊ້າຍ|ໝຸນຂວາ|ຍ້າຍຂຶ້ນ|ຍ້າຍລົງ|ຍ້າຍໄປຊ້າຍ|ຍ້າຍໄປຂວາ|ຄວາມໜາ|ຮູບແບບ|ການຈັດວາງ|ແນວຕັ້ງ|ແນວນອນ|ສີດຳ|ສີຂາວ|ສີແດງ|ສີສົ້ມ|ສີເຫຼືອງ|ສີຂຽວ|ສີຟ້າອ່ອນ|ສີຟ້າ|ສີມ່ວງ|ສີບົວ|ສີເທົາ|ສີນ້ຳຕານ|ສີທອງ|ສີເງິນ",
    "mn" to "Буцах|Зураг нэмэх|Устгах|Томруулах|Жижигрүүлэх|Зүүн тийш эргүүлэх|Баруун тийш эргүүлэх|Дээш зөөх|Доош зөөх|Зүүн тийш зөөх|Баруун тийш зөөх|Зузаан|Формат|Байршил|Босоо|Хэвтээ|Хар|Цагаан|Улаан|Улбар шар|Шар|Ногоон|Цайвар цэнхэр|Цэнхэр|Нил ягаан|Ягаан|Саарал|Бор|Алт|Мөнгө",
    "ky" to "Артка|Сүрөттөрдү кошуу|Өчүрүү|Чоңойтуу|Кичирейтүү|Солго буруу|Оңго буруу|Өйдө жылдыруу|Ылдый жылдыруу|Солго жылдыруу|Оңго жылдыруу|Калыңдыгы|Формат|Жайгашуу|Тик|Туурасынан|Кара|Ак|Кызыл|Кызгылт сары|Сары|Жашыл|Ачык көк|Көк|Күлгүн|Кызгылт|Боз|Күрөң|Алтын|Күмүш",
    "ps" to "شاته|انځورونه اضافه کړئ|ړنګول|لوی کول|وړوکی کول|کیڼ ته څرخول|ښي ته څرخول|پورته کول|ښکته کول|کیڼ ته کول|ښي ته کول|ضخامت|بڼه|ترتیب|عمودي|افقي|تور|سپین|سور|نارنجي|ژېړ|شین|روښانه آبي|آبي|بنفش|ګلابي|خړ|نصواري|زرین|سپینزر",
    "sd" to "پوئتي|تصويرون شامل ڪريو|ڊليٽ ڪريو|وڏو ڪريو|ننڍو ڪريو|کاٻي ڦيرايو|ساڄي ڦيرايو|مٿي کڻو|هيٺ کڻو|کاٻي کڻو|ساڄي کڻو|ٿولهه|فارميٽ|ترتيب|عمودي|افقي|ڪارو|اڇو|ڳاڙهو|نارنگي|پيلو|سائو|هلڪو نيرو|نيرو|واڱڻائي|گلابي|ڀورو|ناسي|سونهري|چانديءَ جهڙو",
    "or" to "ପଛକୁ|ଚିତ୍ର ଯୋଡ଼ନ୍ତୁ|ବିଲୋପ କରନ୍ତୁ|ବଡ଼ କରନ୍ତୁ|ଛୋଟ କରନ୍ତୁ|ବାମକୁ ଘୁରାନ୍ତୁ|ଡାହାଣକୁ ଘୁରାନ୍ତୁ|ଉପରକୁ ନିଅନ୍ତୁ|ତଳକୁ ନିଅନ୍ତୁ|ବାମକୁ ନିଅନ୍ତୁ|ଡାହାଣକୁ ନିଅନ୍ତୁ|ମୋଟେଇ|ଫର୍ମାଟ୍|ଲେଆଉଟ୍|ଭୂଲମ୍ବ|ଆନୁଭୂମିକ|କଳା|ଧଳା|ନାଲି|କମଳା|ହଳଦିଆ|ସବୁଜ|ହାଲୁକା ନୀଳ|ନୀଳ|ବାଇଗଣି|ଗୋଲାପୀ|ଧୂସର|ବାଦାମୀ|ସୁନେଲି|ରୁପେଲି",
    "as" to "পিছলৈ|ছবি যোগ কৰক|মচক|ডাঙৰ কৰক|সৰু কৰক|বাওঁফালে ঘূৰাওক|সোঁফালে ঘূৰাওক|ওপৰলৈ লৈ যাওক|তললৈ লৈ যাওক|বাওঁলৈ লৈ যাওক|সোঁলৈ লৈ যাওক|ঘনত্ব|ফৰ্মেট|বিন্যাস|উলম্ব|অনুভূমিক|ক'লা|বগা|ৰঙা|কমলা|হালধীয়া|সেউজীয়া|পাতল নীলা|নীলা|বেঙুনীয়া|গোলাপী|ধূসৰ|মাটিয়া|সোণালী|ৰূপালী",
    "ceb" to "Balik|Dugangi og mga hulagway|Papason|Padak-a|Pagamyia|Iliso sa wala|Iliso sa tuo|Ibalhin pataas|Ibalhin paubos|Ibalhin sa wala|Ibalhin sa tuo|Gibag-on|Format|Layout|Patindog|Pahigda|Itom|Puti|Pula|Orange|Dalag|Berde|Hayag nga asul|Asul|Lila|Pink|Abo|Brown|Bulawan|Pilak",
    "eu" to "Atzera|Gehitu irudiak|Ezabatu|Handiagoa|Txikiagoa|Biratu ezkerrera|Biratu eskuinera|Mugitu gora|Mugitu behera|Mugitu ezkerrera|Mugitu eskuinera|Lodiera|Formatua|Diseinua|Bertikala|Horizontala|Beltza|Zuria|Gorria|Laranja|Horia|Berdea|Urdin argia|Urdina|Morea|Arrosa|Grisa|Marroia|Urrea|Zilarra",
    "gl" to "Atrás|Engadir imaxes|Eliminar|Máis grande|Máis pequeno|Xirar á esquerda|Xirar á dereita|Mover cara arriba|Mover cara abaixo|Mover á esquerda|Mover á dereita|Grosor|Formato|Deseño|Vertical|Horizontal|Negro|Branco|Vermello|Laranxa|Amarelo|Verde|Azul claro|Azul|Violeta|Rosa|Gris|Marrón|Dourado|Prateado",
    "wuu" to "返回|添加图片|删除|放大|缩小|向左旋转|向右旋转|向上移动|向下移动|向左移动|向右移动|粗细|格式|布局|竖向|横向|黑色|白色|红色|橙色|黄色|绿色|浅蓝色|蓝色|紫色|粉红色|灰色|棕色|金色|银色",
    "bho" to "पीछे|फोटो जोड़ीं|मिटाईं|बड़ करीं|छोट करीं|बाएँ घुमाईं|दाहिने घुमाईं|ऊपर ले जाईं|नीचे ले जाईं|बाएँ ले जाईं|दाहिने ले जाईं|मोटाई|फॉर्मेट|लेआउट|खड़ा|आड़ा|करिया|उज्जर|लाल|नारंगी|पियर|हरियर|हल्का नीला|नीला|बैंगनी|गुलाबी|राख रंग|भूरा|सोनहुला|चाँदी रंग",
    "nan" to "轉去|加圖片|刪掉|放大|縮細|向倒爿轉|向正爿轉|向頂徙|向下徙|向倒爿徙|向正爿徙|粗幼|格式|版面|直的|橫的|烏色|白色|紅色|橘色|黃色|青色|淺藍色|藍色|紫色|粉紅色|灰色|茶色|金色|銀色",
    "om" to "Duubatti deebi'i|Suuraalee dabali|Haqi|Guddisi|Xiqqeessi|Gara bitaatti naanna'i|Gara mirgatti naanna'i|Gara olitti kaasi|Gara gaditti kaasi|Gara bitaatti sosoo'i|Gara mirgatti sosoo'i|Furdina|Bifa|Qindoomina|Vertikaalaa|Horizontaalaa|Gurraacha|Adii|Diimaa|Burtukaana|Keelloo|Magariisa|Buluu ifaa|Buluu|Poorpilii|Pinkii|Boora|Bunaa|Warqee|Meetii",
    "lb" to "Zréck|Biller derbäisetzen|Läschen|Méi grouss|Méi kleng|No lénks dréinen|No riets dréinen|No uewen réckelen|No ënnen réckelen|No lénks réckelen|No riets réckelen|Déckt|Format|Layout|Vertikal|Horizontal|Schwaarz|Wäiss|Rout|Orange|Giel|Gréng|Hellbloen|Bloen|Violett|Rosa|Gro|Brong|Gold|Sëlwer",
    "ga" to "Siar|Cuir íomhánna leis|Scrios|Níos mó|Níos lú|Rothlaigh ar chlé|Rothlaigh ar dheis|Bog suas|Bog síos|Bog ar chlé|Bog ar dheis|Tiús|Formáid|Leagan amach|Ingearach|Cothrománach|Dubh|Bán|Dearg|Oráiste|Buí|Glas|Gorm éadrom|Gorm|Corcra|Bándearg|Liath|Donn|Ór|Airgead",
    "gd" to "Air ais|Cuir dealbhan ris|Sguab às|Nas motha|Nas lugha|Cuairtich gu clì|Cuairtich gu deas|Gluais suas|Gluais sìos|Gluais gu clì|Gluais gu deas|Tiughad|Fòrmat|Cruth-clàraidh|Dìreach|Cothromach|Dubh|Geal|Dearg|Orains|Buidhe|Uaine|Gorm aotrom|Gorm|Purpaidh|Pinc|Glas|Donn|Òr|Airgead",
    "cy" to "Yn ôl|Ychwanegu delweddau|Dileu|Mwy|Llai|Cylchdroi i'r chwith|Cylchdroi i'r dde|Symud i fyny|Symud i lawr|Symud i'r chwith|Symud i'r dde|Trwch|Fformat|Cynllun|Fertigol|Llorweddol|Du|Gwyn|Coch|Oren|Melyn|Gwyrdd|Glas golau|Glas|Porffor|Pinc|Llwyd|Brown|Aur|Arian",
    "br" to "Distreiñ|Ouzhpennañ skeudennoù|Dilemel|Brasoc'h|Bihanoc'h|Kelc'hiañ a-gleiz|Kelc'hiañ a-zehou|Dilec'hiañ war-laez|Dilec'hiañ war-draoñ|Dilec'hiañ a-gleiz|Dilec'hiañ a-zehou|Tevder|Furmad|Aozadur|Vertikal|Horizontal|Du|Gwenn|Ruz|Orañjez|Melen|Gwer|Glas sklaer|Glas|Mouk|Roz|Gris|Brun|Aour|Arc'hant",
    "bs" to "Nazad|Dodaj slike|Izbriši|Veće|Manje|Rotiraj ulijevo|Rotiraj udesno|Pomjeri gore|Pomjeri dolje|Pomjeri ulijevo|Pomjeri udesno|Debljina|Format|Raspored|Okomito|Vodoravno|Crna|Bijela|Crvena|Narandžasta|Žuta|Zelena|Svijetloplava|Plava|Ljubičasta|Roza|Siva|Smeđa|Zlatna|Srebrna",
    "mk" to "Назад|Додај слики|Избриши|Поголемо|Помало|Сврти налево|Сврти надесно|Премести нагоре|Премести надолу|Премести налево|Премести надесно|Дебелина|Формат|Распоред|Вертикално|Хоризонтално|Црна|Бела|Црвена|Портокалова|Жолта|Зелена|Светлосина|Сина|Виолетова|Розова|Сива|Кафеава|Златна|Сребрена",
    "be" to "Назад|Дадаць выявы|Выдаліць|Больш|Менш|Павярнуць улева|Павярнуць управа|Перамясціць уверх|Перамясціць уніз|Перамясціць улева|Перамясціць управа|Таўшчыня|Фармат|Макет|Вертыкальна|Гарызантальна|Чорны|Белы|Чырвоны|Аранжавы|Жоўты|Зялёны|Блакітны|Сіні|Фіялетавы|Ружовы|Шэры|Карычневы|Залаты|Серабрысты",
    "eo" to "Reen|Aldoni bildojn|Forigi|Pli granda|Pli malgranda|Turni maldekstren|Turni dekstren|Movi supren|Movi malsupren|Movi maldekstren|Movi dekstren|Dikeco|Formato|Aranĝo|Vertikala|Horizontala|Nigra|Blanka|Ruĝa|Oranĝa|Flava|Verda|Helblua|Blua|Purpura|Rozkolora|Griza|Bruna|Ora|Arĝenta",
    "mt" to "Lura|Żid immaġini|Ħassar|Akbar|Iżgħar|Dawwar lejn ix-xellug|Dawwar lejn il-lemin|Mexxi 'l fuq|Mexxi 'l isfel|Mexxi lejn ix-xellug|Mexxi lejn il-lemin|Ħxuna|Format|Tqassim|Vertikali|Orizzontali|Iswed|Abjad|Aħmar|Oranġjo|Isfar|Aħdar|Blu ċar|Blu|Vjola|Roża|Griż|Kannella|Deheb|Fidda",
    "la" to "Retro|Imagines adde|Dele|Maius|Minus|Ad sinistram verte|Ad dextram verte|Sursum move|Deorsum move|Ad sinistram move|Ad dextram move|Crassitudo|Forma|Dispositio|Verticalis|Horizontalis|Niger|Albus|Ruber|Aurantiacus|Flavus|Viridis|Caeruleus clarus|Caeruleus|Purpureus|Roseus|Cinereus|Fuscus|Aureus|Argenteus",
    "mai" to "पाछाँ|चित्र जोड़ू|मेटाउ|पैघ करू|छोट करू|बामा घुमाउ|दहिना घुमाउ|ऊपर सरकाउ|नीचाँ सरकाउ|बामा सरकाउ|दहिना सरकाउ|मोटाइ|फॉर्मेट|लेआउट|ठाढ़|पड़ल|करिया|उजरा|लाल|नारंगी|पीयर|हरियर|हल्लुक नील|नील|बैंगनी|गुलाबी|धूसर|भूरा|सोनाक रंग|चानीक रंग",
    "sr-Latn" to "Nazad|Dodaj slike|Obriši|Veće|Manje|Okreni ulevo|Okreni udesno|Pomeri nagore|Pomeri nadole|Pomeri ulevo|Pomeri udesno|Debljina|Format|Raspored|Vertikalno|Horizontalno|Crna|Bela|Crvena|Narandžasta|Žuta|Zelena|Svetloplava|Plava|Ljubičasta|Roze|Siva|Braon|Zlatna|Srebrna",
    "pt-PT" to "Voltar|Adicionar imagens|Eliminar|Maior|Menor|Rodar para a esquerda|Rodar para a direita|Mover para cima|Mover para baixo|Mover para a esquerda|Mover para a direita|Espessura|Formato|Esquema|Vertical|Horizontal|Preto|Branco|Vermelho|Laranja|Amarelo|Verde|Azul-claro|Azul|Roxo|Cor-de-rosa|Cinzento|Castanho|Dourado|Prateado",
    "nn" to "Tilbake|Legg til bilete|Slett|Større|Mindre|Roter til venstre|Roter til høgre|Flytt opp|Flytt ned|Flytt til venstre|Flytt til høgre|Tjukkleik|Format|Oppsett|Loddrett|Vassrett|Svart|Kvit|Raud|Oransje|Gul|Grøn|Lyseblå|Blå|Lilla|Rosa|Grå|Brun|Gull|Sølv",
    "kok" to "मागीर|चित्रां जोडात|काडून उडयात|व्हड करात|ल्हान करात|दावे वटेन फिरयात|उजवे वटेन फिरयात|वयर व्हरात|सकयल व्हरात|दावे वटेन व्हरात|उजवे वटेन व्हरात|जाडेपण|फॉरमॅट|मांडावळ|उबो|आडवो|काळो|धवो|तांबडो|नारंगी|पिवळो|हिरवो|फिकट निळो|निळो|जांबळो|गुलाबी|करडो|खाकी|सोनेरी|रुपेरी",
    // ENDE-DER-DATEN
)
