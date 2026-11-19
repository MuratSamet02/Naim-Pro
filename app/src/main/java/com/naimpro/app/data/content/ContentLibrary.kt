package com.naimpro.app.data.content

/** A short piece of daily content with its source. */
data class ContentItem(
    val id: String,
    val arabic: String,
    val text: String,
    val source: String,
)

/**
 * Offline content library: daily prayers, Qur'anic verses and hadith.
 *
 * * The Qur'anic verses use short excerpts from Elmalılı Hamdi Yazır's Turkish
 *   translation (1878-1942), which is in the public domain.
 * * The hadith texts are original Turkish paraphrases of well known narrations
 *   and cite the classical collection they come from.
 * * The prayers (dua) are the classical supplications of the public domain
 *   Islamic tradition.
 */
object ContentLibrary {
    val duas: List<ContentItem> =
        listOf(
            ContentItem(
                "dua_1",
                "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً",
                "Rabbimiz! Bize dünyada da iyilik ver, ahirette de iyilik ver ve bizi cehennem azabından koru.",
                "Bakara 2/201",
            ),
            ContentItem("dua_2", "رَبِّ زِدْنِي عِلْمًا", "Rabbim! İlmimi artır.", "Tâhâ 20/114"),
            ContentItem(
                "dua_3",
                "رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا",
                "Rabbimiz! Bizi hidayete erdirdikten sonra kalplerimizi kaydırma.",
                "Âl-i İmrân 3/8",
            ),
            ContentItem(
                "dua_4",
                "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي",
                "Rabbim! Gönlüme genişlik ver, işimi kolaylaştır.",
                "Tâhâ 20/25-26",
            ),
            ContentItem(
                "dua_5",
                "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْهُدَى وَالتُّقَى وَالْعَفَافَ وَالْغِنَى",
                "Allah'ım! Senden hidayet, takva, iffet ve gönül zenginliği dilerim.",
                "Müslim, Dua 72",
            ),
            ContentItem(
                "dua_6",
                "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ",
                "Allah'ım! Seni anmak, sana şükretmek ve sana güzelce kulluk etmek için bana yardım et.",
                "Ebû Dâvûd, Vitr 26",
            ),
            ContentItem(
                "dua_7",
                "رَبَّنَا هَبْ لَنَا مِنْ أَزْوَاجِنَا وَذُرِّيَّاتِنَا قُرَّةَ أَعْيُنٍ",
                "Rabbimiz! Eşlerimizi ve çocuklarımızı bize göz aydınlığı kıl.",
                "Furkân 25/74",
            ),
            ContentItem(
                "dua_8",
                "رَبِّ اجْعَلْنِي مُقِيمَ الصَّلَاةِ وَمِنْ ذُرِّيَّتِي",
                "Rabbim! Beni ve soyumdan gelenleri namazı devamlı kılanlardan eyle.",
                "İbrâhim 14/40",
            ),
            ContentItem(
                "dua_9",
                "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ",
                "Allah'ım! Üzüntüden ve kederden sana sığınırım.",
                "Buhârî, Deavât 36",
            ),
            ContentItem("dua_10", "حَسْبُنَا اللّٰهُ وَنِعْمَ الْوَكِيلُ", "Allah bize yeter, O ne güzel vekildir.", "Âl-i İmrân 3/173"),
            ContentItem(
                "dua_11",
                "رَبَّنَا اغْفِرْ لَنَا ذُنُوبَنَا وَإِسْرَافَنَا فِي أَمْرِنَا",
                "Rabbimiz! Günahlarımızı ve işimizdeki taşkınlıklarımızı bağışla.",
                "Âl-i İmrân 3/147",
            ),
            ContentItem("dua_12", "اللَّهُمَّ عَافِنِي فِي بَدَنِي", "Allah'ım! Bedenime sağlık ve afiyet ver.", "Ebû Dâvûd, Edeb 101"),
            ContentItem(
                "dua_13",
                "رَبِّ أَعُوذُ بِكَ مِنْ هَمَزَاتِ الشَّيَاطِينِ",
                "Rabbim! Şeytanların vesveselerinden sana sığınırım.",
                "Mü'minûn 23/97",
            ),
            ContentItem(
                "dua_14",
                "اللَّهُمَّ بَارِكْ لَنَا فِيمَا رَزَقْتَنَا",
                "Allah'ım! Bize verdiğin rızka bereket ihsan eyle.",
                "Klasik âdet duası",
            ),
            ContentItem(
                "dua_15",
                "رَبَّنَا تَقَبَّلْ مِنَّا إِنَّكَ أَنْتَ السَّمِيعُ الْعَلِيمُ",
                "Rabbimiz! Bizden kabul buyur; şüphesiz Sen işitensin, bilensin.",
                "Bakara 2/127",
            ),
            ContentItem(
                "dua_16",
                "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْجَنَّةَ وَأَعُوذُ بِكَ مِنَ النَّارِ",
                "Allah'ım! Senden cenneti istiyorum, cehennemden sana sığınıyorum.",
                "Ebû Dâvûd, Salât 358",
            ),
            ContentItem(
                "dua_17",
                "رَبِّ لَا تَذَرْنِي فَرْدًا وَأَنْتَ خَيْرُ الْوَارِثِينَ",
                "Rabbim! Beni tek başıma bırakma; Sen vârislerin en hayırlısısın.",
                "Enbiyâ 21/89",
            ),
            ContentItem(
                "dua_18",
                "اللَّهُمَّ اهْدِنِي وَسَدِّدْنِي",
                "Allah'ım! Beni doğru yola ilet ve isabetli kıl.",
                "Müslim, İmâre 46",
            ),
            ContentItem(
                "dua_19",
                "رَبَّنَا آمَنَّا فَاغْفِرْ لَنَا وَارْحَمْنَا",
                "Rabbimiz! İman ettik; bizi bağışla ve bize merhamet et.",
                "Mü'minûn 23/109",
            ),
            ContentItem(
                "dua_20",
                "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ عِلْمٍ لَا يَنْفَعُ",
                "Allah'ım! Faydasız ilimden sana sığınırım.",
                "Müslim, Zikir 73",
            ),
            ContentItem("dua_21", "رَبَّنَا أَفْرِغْ عَلَيْنَا صَبْرًا", "Rabbimiz! Üzerimize sabır yağdır.", "Bakara 2/250"),
            ContentItem(
                "dua_22",
                "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ",
                "Ey kalpleri çeviren Allah'ım! Kalbimi dinin üzerinde sabit kıl.",
                "Tirmizî, Kader 7",
            ),
            ContentItem(
                "dua_23",
                "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا وَرِزْقًا طَيِّبًا",
                "Allah'ım! Senden faydalı ilim ve temiz rızık dilerim.",
                "İbn Mâce, İkâme 32",
            ),
            ContentItem(
                "dua_24",
                "رَبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
                "Rabbim! Onlar beni küçüklüğümde nasıl büyüttüyse, sen de onlara merhamet et.",
                "İsrâ 17/24",
            ),
            ContentItem(
                "dua_25",
                "اللَّهُمَّ اجْعَلْ أَوَّلَ هٰذَا النَّهَارِ صَلَاحًا",
                "Allah'ım! Bu günün başlangıcını hayır, ortasını kurtuluş, sonunu bağışlanma eyle.",
                "Klasik sabah duası",
            ),
            ContentItem(
                "dua_26",
                "رَبَّنَا وَلَا تُحَمِّلْنَا مَا لَا طَاقَةَ لَنَا بِهِ",
                "Rabbimiz! Gücümüzün yetmeyeceği yükü bize yükleme.",
                "Bakara 2/286",
            ),
            ContentItem("dua_27", "اللَّهُمَّ أَجِرْنِي مِنَ النَّارِ", "Allah'ım! Beni cehennem ateşinden koru.", "Ebû Dâvûd, Salât 359"),
            ContentItem(
                "dua_28",
                "رَبِّ أَوْزِعْنِي أَنْ أَشْكُرَ نِعْمَتَكَ",
                "Rabbim! Bana verdiğin nimetlere şükretmeyi bana ilham et.",
                "Neml 27/19",
            ),
            ContentItem("dua_29", "اللَّهُمَّ طَهِّرْ قَلْبِي مِنَ النِّفَاقِ", "Allah'ım! Kalbimi nifaktan temizle.", "Klasik dua"),
            ContentItem(
                "dua_30",
                "رَبَّنَا اصْرِفْ عَنَّا عَذَابَ جَهَنَّمَ",
                "Rabbimiz! Cehennem azabını bizden uzaklaştır.",
                "Furkân 25/65",
            ),
        )

    val ayets: List<ContentItem> =
        listOf(
            ContentItem("ayet_1", "إِنَّ مَعَ الْعُسْرِ يُسْرًا", "Şüphesiz güçlükle beraber bir kolaylık vardır.", "İnşirâh 94/6"),
            ContentItem(
                "ayet_2",
                "وَمَنْ يَتَوَكَّلْ عَلَى اللّٰهِ فَهُوَ حَسْبُهُ",
                "Kim Allah'a tevekkül ederse, O ona kâfidir.",
                "Talâk 65/3",
            ),
            ContentItem(
                "ayet_3",
                "أَلَا بِذِكْرِ اللّٰهِ تَطْمَئِنُّ الْقُلُوبُ",
                "Bilesiniz ki kalpler ancak Allah'ı anmakla huzur bulur.",
                "Ra'd 13/28",
            ),
            ContentItem(
                "ayet_4",
                "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا",
                "O halde her güçlükle beraber mutlaka bir kolaylık vardır.",
                "İnşirâh 94/5",
            ),
            ContentItem(
                "ayet_5",
                "وَعَسٰى أَنْ تَكْرَهُوا شَيْئًا وَهُوَ خَيْرٌ لَكُمْ",
                "Olur ki hoşunuza gitmeyen bir şey sizin için hayırlıdır.",
                "Bakara 2/216",
            ),
            ContentItem(
                "ayet_6",
                "لَا يُكَلِّفُ اللّٰهُ نَفْسًا إِلَّا وُسْعَهَا",
                "Allah hiç kimseye gücünün üstünde bir şey yüklemez.",
                "Bakara 2/286",
            ),
            ContentItem("ayet_7", "وَقُلْ رَبِّ زِدْنِي عِلْمًا", "De ki: Rabbim! İlmimi artır.", "Tâhâ 20/114"),
            ContentItem("ayet_8", "إِنَّ اللّٰهَ مَعَ الصَّابِرِينَ", "Muhakkak ki Allah sabredenlerle beraberdir.", "Bakara 2/153"),
            ContentItem("ayet_9", "وَهُوَ مَعَكُمْ أَيْنَ مَا كُنْتُمْ", "Nerede olsanız O sizinle beraberdir.", "Hadîd 57/4"),
            ContentItem("ayet_10", "فَاذْكُرُونِي أَذْكُرْكُمْ", "Öyleyse beni anın, ben de sizi anayım.", "Bakara 2/152"),
            ContentItem(
                "ayet_11",
                "إِنَّ الصَّلَاةَ تَنْهٰى عَنِ الْفَحْشَاءِ وَالْمُنْكَرِ",
                "Şüphesiz namaz hayâsızlıktan ve kötülükten alıkoyar.",
                "Ankebût 29/45",
            ),
            ContentItem(
                "ayet_12",
                "وَمَنْ أَحْسَنُ قَوْلًا مِمَّنْ دَعَا إِلَى اللّٰهِ",
                "Allah'a davet edenden daha güzel sözlü kim olabilir?",
                "Fussilet 41/33",
            ),
            ContentItem(
                "ayet_13",
                "وَمَا خَلَقْتُ الْجِنَّ وَالْإِنْسَ إِلَّا لِيَعْبُدُونِ",
                "Ben cinleri ve insanları ancak bana kulluk etsinler diye yarattım.",
                "Zâriyât 51/56",
            ),
            ContentItem("ayet_14", "وَلَسَوْفَ يُعْطِيكَ رَبُّكَ فَتَرْضٰى", "Rabbin sana verecek ve sen hoşnut olacaksın.", "Duhâ 93/5"),
            ContentItem(
                "ayet_15",
                "إِنَّ رَحْمَتَ اللّٰهِ قَرِيبٌ مِنَ الْمُحْسِنِينَ",
                "Muhakkak Allah'ın rahmeti iyilik edenlere çok yakındır.",
                "A'râf 7/56",
            ),
            ContentItem("ayet_16", "وَاللّٰهُ يُحِبُّ الصَّابِرِينَ", "Allah sabredenleri sever.", "Âl-i İmrân 3/146"),
            ContentItem("ayet_17", "تَعَالَى اللّٰهُ الْمَلِكُ الْحَقُّ", "Gerçek hükümdar olan Allah yücedir.", "Tâhâ 20/114"),
            ContentItem(
                "ayet_18",
                "إِنَّ اللّٰهَ لَا يُضِيعُ أَجْرَ الْمُحْسِنِينَ",
                "Şüphesiz Allah iyilik edenlerin ecrini kaybetmez.",
                "Tevbe 9/120",
            ),
            ContentItem(
                "ayet_19",
                "وَأَقِيمُوا الصَّلَاةَ وَلَا تَكُونُوا مِنَ الْمُشْرِكِينَ",
                "Namazı dosdoğru kılın ve müşriklerden olmayın.",
                "Rûm 30/31",
            ),
            ContentItem(
                "ayet_20",
                "رَبِّ اغْفِرْ وَارْحَمْ وَأَنْتَ خَيْرُ الرَّاحِمِينَ",
                "Rabbim! Bağışla, merhamet et; Sen merhamet edenlerin en hayırlısısın.",
                "Mü'minûn 23/118",
            ),
            ContentItem(
                "ayet_21",
                "لَئِنْ شَكَرْتُمْ لَأَزِيدَنَّكُمْ",
                "Andolsun, eğer şükrederseniz elbette size nimetimi artırırım.",
                "İbrâhim 14/7",
            ),
            ContentItem("ayet_22", "وَلَا تَيْأَسُوا مِنْ رَوْحِ اللّٰهِ", "Allah'ın rahmetinden ümidinizi kesmeyin.", "Yûsuf 12/87"),
            ContentItem(
                "ayet_23",
                "وَأَنْ لَيْسَ لِلْإِنْسَانِ إِلَّا مَا سَعٰى",
                "İnsan için ancak çalıştığının karşılığı vardır.",
                "Necm 53/39",
            ),
            ContentItem(
                "ayet_24",
                "وَالْعَصْرِ إِنَّ الْإِنْسَانَ لَفِي خُسْرٍ",
                "Asra yemin olsun, insan gerçekten ziyan içindedir.",
                "Asr 103/1-2",
            ),
            ContentItem("ayet_25", "فَاصْبِرْ إِنَّ وَعْدَ اللّٰهِ حَقٌّ", "Sabret! Şüphesiz Allah'ın vaadi haktır.", "Rûm 30/60"),
        )

    val hadiths: List<ContentItem> =
        listOf(
            ContentItem(
                "hadis_1",
                "إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ",
                "Ameller niyetlere göredir; herkese ancak niyet ettiği vardır.",
                "Buhârî, Bed'ü'l-vahy 1",
            ),
            ContentItem("hadis_2", "الدِّينُ النَّصِيحَةُ", "Din samimiyettir, iyi niyet ve dürüstlüktür.", "Müslim, Îmân 95"),
            ContentItem(
                "hadis_3",
                "مَنْ كَانَ يُؤْمِنُ بِاللّٰهِ وَالْيَوْمِ الْاٰخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ",
                "Allah'a ve ahiret gününe inanan ya hayır söylesin ya da sussun.",
                "Buhârî, Edeb 31",
            ),
            ContentItem(
                "hadis_4",
                "لَا يُؤْمِنُ أَحَدُكُمْ حَتّٰى يُحِبَّ لِأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ",
                "Sizden biri kendisi için sevdiğini kardeşi için de sevmedikçe iman etmiş olmaz.",
                "Buhârî, Îmân 7",
            ),
            ContentItem(
                "hadis_5",
                "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
                "Sizin en hayırlınız Kur'an'ı öğrenen ve öğretendir.",
                "Buhârî, Fedâilü'l-Kur'ân 21",
            ),
            ContentItem("hadis_6", "الطُّهُورُ شَطْرُ الْإِيمَانِ", "Temizlik imanın yarısıdır.", "Müslim, Tahâret 1"),
            ContentItem(
                "hadis_7",
                "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللّٰهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ",
                "İlim öğrenmek için yola çıkana Allah cennete giden yolu kolaylaştırır.",
                "Müslim, Zikir 38",
            ),
            ContentItem(
                "hadis_8",
                "الْمُسْلِمُ مَنْ سَلِمَ الْمُسْلِمُونَ مِنْ لِسَانِهِ وَيَدِهِ",
                "Müslüman, dilinden ve elinden Müslümanların güvende olduğu kimsedir.",
                "Buhârî, Îmân 4",
            ),
            ContentItem("hadis_9", "اتَّقِ اللّٰهَ حَيْثُمَا كُنْتَ", "Nerede olursan ol Allah'tan kork ve sakın.", "Tirmizî, Birr 55"),
            ContentItem(
                "hadis_10",
                "إِنَّ الرِّفْقَ لَا يَكُونُ فِي شَيْءٍ إِلَّا زَانَهُ",
                "Yumuşaklık girdiği her şeyi güzelleştirir.",
                "Müslim, Birr 77",
            ),
            ContentItem("hadis_11", "مَنْ لَا يَرْحَمُ لَا يُرْحَمُ", "İnsanlara merhamet etmeyene merhamet edilmez.", "Buhârî, Edeb 18"),
            ContentItem(
                "hadis_12",
                "تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ",
                "Kardeşine gülümsemen senin için bir sadakadır.",
                "Tirmizî, Birr 36",
            ),
            ContentItem("hadis_13", "الصَّلَاةُ نُورٌ", "Namaz nurdur.", "Müslim, Tahâret 1"),
            ContentItem("hadis_14", "الدُّعَاءُ هُوَ الْعِبَادَةُ", "Dua ibadetin özüdür.", "Tirmizî, Deavât 1"),
            ContentItem("hadis_15", "الْكَلِمَةُ الطَّيِّبَةُ صَدَقَةٌ", "Güzel söz sadakadır.", "Buhârî, Cihâd 128"),
            ContentItem(
                "hadis_16",
                "إِنَّ اللّٰهَ جَمِيلٌ يُحِبُّ الْجَمَالَ",
                "Muhakkak Allah güzeldir, güzelliği sever.",
                "Müslim, Îmân 147",
            ),
            ContentItem("hadis_17", "لَا تَغْضَبْ", "Öfkelenme.", "Buhârî, Edeb 76"),
            ContentItem("hadis_18", "أَفْشُوا السَّلَامَ بَيْنَكُمْ", "Aranızda selamı yaygınlaştırın.", "Müslim, Îmân 93"),
            ContentItem(
                "hadis_19",
                "مَنْ صَلَّى عَلَيَّ صَلَّى اللّٰهُ عَلَيْهِ عَشْرًا",
                "Bana bir salavat getirene Allah on rahmet eder.",
                "Müslim, Cum'a 70",
            ),
            ContentItem(
                "hadis_20",
                "خَيْرُ الصَّدَقَةِ مَا كَانَ عَنْ ظَهْرِ غِنًى",
                "En hayırlı sadaka, ihtiyaçtan arta kalıp başkasına yetecek olandır.",
                "Buhârî, Zekât 18",
            ),
            ContentItem(
                "hadis_21",
                "إِنَّ اللّٰهَ لَا يَنْظُرُ إِلٰى صُوَرِكُمْ وَلٰكِنْ يَنْظُرُ إِلٰى قُلُوبِكُمْ",
                "Allah sizin suretlerinize ve mallarınıza bakmaz, kalplerinize ve amellerinize bakar.",
                "Müslim, Birr 34",
            ),
            ContentItem("hadis_22", "كُلُّ مَعْرُوفٍ صَدَقَةٌ", "Her iyilik bir sadakadır.", "Buhârî, Edeb 33"),
            ContentItem(
                "hadis_23",
                "مَنْ حُسْنِ إِسْلَامِ الْمَرْءِ تَرْكُهُ مَا لَا يَعْنِيهِ",
                "Bir Müslüman'ın olgunluğu, kendisini ilgilendirmeyen şeyi bırakmasındandır.",
                "Tirmizî, Zühd 11",
            ),
            ContentItem(
                "hadis_24",
                "احْفَظِ اللّٰهَ يَحْفَظْكَ",
                "Allah'ın emirlerini koru ki Allah da seni korusun.",
                "Tirmizî, Kıyâme 59",
            ),
            ContentItem(
                "hadis_25",
                "بَلِّغُوا عَنِّي وَلَوْ آيَةً",
                "Benden bir ayet bile olsa tebliğ edin, ulaştırın.",
                "Buhârî, Enbiyâ 50",
            ),
        )

    fun allDuas(): List<ContentItem> = duas

    fun allAyets(): List<ContentItem> = ayets

    fun allHadiths(): List<ContentItem> = hadiths

    /** Deterministic daily rotation, so the same day always shows the same item. */
    fun dailyDuas(daySeed: Int): ContentItem = duas[daySeed.mod(duas.size)]

    fun dailyAyet(daySeed: Int): ContentItem = ayets[daySeed.mod(ayets.size)]

    fun dailyHadith(daySeed: Int): ContentItem = hadiths[daySeed.mod(hadiths.size)]
}
