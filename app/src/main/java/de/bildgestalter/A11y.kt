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
    // ENDE-DER-DATEN
)
