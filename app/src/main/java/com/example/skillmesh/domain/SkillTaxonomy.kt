package com.example.skillmesh.domain

object SkillTaxonomy {
    private val synonyms: Map<String, String> = mapOf(
        // Mobile & Languages
        "android" to "Android",
        "android dev" to "Android",
        "android sdk" to "Android",
        "kotlin" to "Kotlin",
        "jetpack compose" to "Jetpack Compose",
        "compose" to "Jetpack Compose",
        "flutter" to "Flutter",
        "dart" to "Flutter",
        "react native" to "React Native",
        "swift" to "iOS",
        "ios" to "iOS",

        // AI / ML / Vision
        "cv" to "Computer Vision",
        "computer vision" to "Computer Vision",
        "opencv" to "Computer Vision",
        "vision" to "Computer Vision",
        "image processing" to "Computer Vision",
        "yolo" to "Computer Vision",
        "ml" to "Machine Learning",
        "machine learning" to "Machine Learning",
        "deep learning" to "Deep Learning",
        "dl" to "Deep Learning",
        "nlp" to "Natural Language Processing",
        "natural language processing" to "Natural Language Processing",
        "llm" to "Large Language Models",
        "large language models" to "Large Language Models",
        "genai" to "Generative AI",
        "generative ai" to "Generative AI",
        "tensorflow" to "TensorFlow",
        "tf" to "TensorFlow",
        "pytorch" to "PyTorch",
        "keras" to "TensorFlow",
        "tflite" to "Edge AI",
        "edge ai" to "Edge AI",
        "on device ai" to "Edge AI",
        "on-device ai" to "Edge AI",
        "mediapipe" to "Edge AI",
        "data science" to "Data Science",
        "pandas" to "Data Science",
        "numpy" to "Data Science",

        // Hardware / IoT / Embedded
        "iot" to "IoT",
        "internet of things" to "IoT",
        "arduino" to "IoT",
        "esp32" to "IoT",
        "raspberry pi" to "IoT",
        "embedded" to "Embedded Systems",
        "embedded systems" to "Embedded Systems",
        "c" to "C/C++",
        "c++" to "C/C++",
        "cpp" to "C/C++",
        "robotics" to "Robotics",
        "ros" to "Robotics",

        // Design & Product
        "ui" to "UI/UX",
        "ux" to "UI/UX",
        "ui/ux" to "UI/UX",
        "ui ux" to "UI/UX",
        "figma" to "UI/UX",
        "product design" to "UI/UX",
        "design system" to "UI/UX",
        "wireframing" to "UI/UX",

        // Backend & Cloud & DB
        "backend" to "Backend",
        "node" to "Node.js",
        "nodejs" to "Node.js",
        "node.js" to "Node.js",
        "python" to "Python",
        "golang" to "Go",
        "go" to "Go",
        "rust" to "Rust",
        "java" to "Java",
        "sql" to "SQL",
        "postgresql" to "PostgreSQL",
        "postgres" to "PostgreSQL",
        "mongodb" to "MongoDB",
        "firebase" to "Firebase",
        "docker" to "Docker",
        "kubernetes" to "Kubernetes",
        "k8s" to "Kubernetes",
        "graphql" to "GraphQL",
        "rest api" to "REST APIs",
        "rest" to "REST APIs",

        // Security & Networking
        "cybersecurity" to "Cybersecurity",
        "security" to "Cybersecurity",
        "infosec" to "Cybersecurity",
        "cryptography" to "Cryptography",
        "crypto" to "Cryptography",
        "mesh networks" to "Mesh Networks",
        "mesh" to "Mesh Networks",
        "bluetooth" to "Bluetooth",
        "ble" to "Bluetooth",
        "wifi direct" to "Wi-Fi Direct",
        "nearby connections" to "Nearby Connections"
    )

    // partial credit between related skills (both directions)
    private val related: Map<Pair<String, String>, Double> = mapOf(
        ("Flutter" to "Android") to 0.5,
        ("Kotlin" to "Android") to 0.7,
        ("Jetpack Compose" to "Android") to 0.8,
        ("React Native" to "Android") to 0.5,
        ("TensorFlow" to "Machine Learning") to 0.7,
        ("PyTorch" to "Machine Learning") to 0.7,
        ("Edge AI" to "Machine Learning") to 0.6,
        ("Edge AI" to "TensorFlow") to 0.6,
        ("Computer Vision" to "Machine Learning") to 0.6,
        ("Deep Learning" to "Machine Learning") to 0.8,
        ("Deep Learning" to "Computer Vision") to 0.6,
        ("Generative AI" to "Large Language Models") to 0.8,
        ("Large Language Models" to "Natural Language Processing") to 0.7,
        ("Data Science" to "Machine Learning") to 0.6,
        ("Python" to "Machine Learning") to 0.5,
        ("Python" to "Data Science") to 0.6,
        ("Python" to "Backend") to 0.5,
        ("IoT" to "Embedded Systems") to 0.8,
        ("IoT" to "Robotics") to 0.6,
        ("C/C++" to "Embedded Systems") to 0.7,
        ("Edge AI" to "IoT") to 0.5,
        ("UI/UX" to "Figma") to 0.8,
        ("Node.js" to "Backend") to 0.7,
        ("Firebase" to "Backend") to 0.6,
        ("PostgreSQL" to "SQL") to 0.8,
        ("Bluetooth" to "Mesh Networks") to 0.6,
        ("Nearby Connections" to "Bluetooth") to 0.7,
        ("Nearby Connections" to "Mesh Networks") to 0.7,
        ("Cybersecurity" to "Cryptography") to 0.7
    )

    val allCanonicalSkills: List<String> = synonyms.values.distinct().sorted()

    fun canonicalize(token: String): String? = synonyms[token.trim().lowercase()]

    fun extractFromText(text: String): List<String> {
        val matches = mutableListOf<String>()
        // Sort keys by descending length so multi-word tokens match first
        val sortedEntries = synonyms.entries.sortedByDescending { it.key.length }
        for ((phrase, canonical) in sortedEntries) {
            val pattern = "(?i)(^|[^a-zA-Z0-9_#+.-])${Regex.escape(phrase)}($|[^a-zA-Z0-9_#+.-])"
            if (Regex(pattern).containsMatchIn(text)) {
                if (!matches.contains(canonical)) {
                    matches.add(canonical)
                }
            }
        }
        return matches
    }

    fun similarity(a: String, b: String): Double {
        if (a.equals(b, ignoreCase = true)) return 1.0
        return related[a to b] ?: related[b to a] ?: 0.0
    }
}
