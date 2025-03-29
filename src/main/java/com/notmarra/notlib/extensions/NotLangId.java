package com.notmarra.notlib.extensions;

public enum NotLangId {
    EN("en"),      // English
    ES("es"),      // Spanish
    FR("fr"),      // French
    DE("de"),      // German
    IT("it"),      // Italian
    PT("pt"),      // Portuguese
    PTBR("ptbr"),  // Brazilian Portuguese (legacy)
    RU("ru"),      // Russian
    ZH("zh"),      // Chinese
    ZHCN("zhcn"),  // Simplified Chinese (legacy)
    JA("ja"),      // Japanese
    
    AF("af"),      // Afrikaans
    SQ("sq"),      // Albanian
    AM("am"),      // Amharic
    AR("ar"),      // Arabic
    HY("hy"),      // Armenian
    AZ("az"),      // Azerbaijani
    EU("eu"),      // Basque
    BE("be"),      // Belarusian
    BN("bn"),      // Bengali
    BS("bs"),      // Bosnian
    BG("bg"),      // Bulgarian
    CA("ca"),      // Catalan
    CEB("ceb"),    // Cebuano
    NY("ny"),      // Chichewa
    CO("co"),      // Corsican
    HR("hr"),      // Croatian
    CS("cs"),      // Czech
    CZ("cz"),      // Czech (legacy)
    DA("da"),      // Danish
    NL("nl"),      // Dutch
    EO("eo"),      // Esperanto
    ET("et"),      // Estonian
    TL("tl"),      // Filipino
    FI("fi"),      // Finnish
    FY("fy"),      // Frisian
    GL("gl"),      // Galician
    KA("ka"),      // Georgian
    EL("el"),      // Greek
    GU("gu"),      // Gujarati
    HT("ht"),      // Haitian Creole
    HA("ha"),      // Hausa
    HAW("haw"),    // Hawaiian
    IW("iw"),      // Hebrew (legacy code, also known as he)
    HE("he"),      // Hebrew
    HI("hi"),      // Hindi
    HMN("hmn"),    // Hmong
    HU("hu"),      // Hungarian
    IS("is"),      // Icelandic
    IG("ig"),      // Igbo
    ID("id"),      // Indonesian
    GA("ga"),      // Irish
    JW("jw"),      // Javanese
    KN("kn"),      // Kannada
    KK("kk"),      // Kazakh
    KM("km"),      // Khmer
    KO("ko"),      // Korean
    KU("ku"),      // Kurdish
    KY("ky"),      // Kyrgyz
    LO("lo"),      // Lao
    LA("la"),      // Latin
    LV("lv"),      // Latvian
    LT("lt"),      // Lithuanian
    LB("lb"),      // Luxembourgish
    MK("mk"),      // Macedonian
    MG("mg"),      // Malagasy
    MS("ms"),      // Malay
    ML("ml"),      // Malayalam
    MT("mt"),      // Maltese
    MI("mi"),      // Maori
    MR("mr"),      // Marathi
    MN("mn"),      // Mongolian
    MY("my"),      // Myanmar (Burmese)
    NE("ne"),      // Nepali
    NO("no"),      // Norwegian
    PS("ps"),      // Pashto
    FA("fa"),      // Persian
    PL("pl"),      // Polish
    PA("pa"),      // Punjabi
    RO("ro"),      // Romanian
    SM("sm"),      // Samoan
    GD("gd"),      // Scots Gaelic
    SR("sr"),      // Serbian
    ST("st"),      // Sesotho
    SN("sn"),      // Shona
    SD("sd"),      // Sindhi
    SI("si"),      // Sinhala
    SK("sk"),      // Slovak
    SL("sl"),      // Slovenian
    SO("so"),      // Somali
    SU("su"),      // Sundanese
    SW("sw"),      // Swahili
    SV("sv"),      // Swedish
    TG("tg"),      // Tajik
    TA("ta"),      // Tamil
    TE("te"),      // Telugu
    TH("th"),      // Thai
    TR("tr"),      // Turkish
    UK("uk"),      // Ukrainian
    UR("ur"),      // Urdu
    UZ("uz"),      // Uzbek
    VI("vi"),      // Vietnamese
    CY("cy"),      // Welsh
    XH("xh"),      // Xhosa
    YI("yi"),      // Yiddish
    YO("yo"),      // Yoruba
    ZU("zu");      // Zulu

    private final String langCode;

    NotLangId(String langCode) {
        this.langCode = langCode;
    }

    public String getLangCode() {
        return langCode;
    }

    public static NotLangId fromCode(String code) {
        for (NotLangId lang : values()) {
            if (lang.getLangCode().equalsIgnoreCase(code)) {
                return lang;
            }
        }
        return EN;
    }
}