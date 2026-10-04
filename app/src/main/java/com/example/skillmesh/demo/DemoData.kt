package com.example.skillmesh.demo

import com.example.skillmesh.domain.Profile
import com.example.skillmesh.domain.Project
import com.example.skillmesh.domain.ProjectTask

object DemoData {

    val people: List<Profile> = listOf(
        Profile(
            id = "demo-1",
            name = "Ananya Sharma",
            bio = "Mobile systems engineer specializing in low-latency camera pipelines and on-device vision models.",
            skills = listOf("Android", "Computer Vision", "Flutter", "Kotlin"),
            interests = listOf("Robotics", "Edge Computing", "Open Source"),
            experience = "Advanced",
            available = true
        ),
        Profile(
            id = "demo-2",
            name = "Rahul Verma",
            bio = "Embedded firmware & TinyML hacker running neural nets on microcontrollers and edge hardware.",
            skills = listOf("Edge AI", "TensorFlow", "IoT", "Embedded Systems"),
            interests = listOf("Smart Cities", "Hardware Prototyping"),
            experience = "Advanced",
            available = true
        ),
        Profile(
            id = "demo-3",
            name = "Priya Patel",
            bio = "Design technologist crafting micro-interactions, dark mode design systems, and mobile UI flows in Figma.",
            skills = listOf("UI/UX", "Figma", "Design Systems"),
            interests = listOf("Accessibility", "Motion Graphics", "Product Design"),
            experience = "Intermediate",
            available = true
        ),
        Profile(
            id = "demo-4",
            name = "Karan Malhotra",
            bio = "Backend and distributed data engineer focusing on resilient APIs and asynchronous data pipelines.",
            skills = listOf("Python", "Machine Learning", "Backend", "PostgreSQL"),
            interests = listOf("Distributed Systems", "Data Science"),
            experience = "Advanced",
            available = true
        ),
        Profile(
            id = "demo-5",
            name = "Neha Gupta",
            bio = "Frontend & Cross-platform specialist with a strong eye for clean interfaces and animations.",
            skills = listOf("React Native", "UI/UX", "JavaScript"),
            interests = listOf("Mobile Tech", "Civic Tech"),
            experience = "Intermediate",
            available = true
        ),
        Profile(
            id = "demo-6",
            name = "Devansh Joshi",
            bio = "Security enthusiast researching peer-to-peer protocols, zero-knowledge proofs, and offline cryptographic handshakes.",
            skills = listOf("Cybersecurity", "Cryptography", "Mesh Networks"),
            interests = listOf("Privacy", "Decentralized Web"),
            experience = "Intermediate",
            available = false
        ),
        Profile(
            id = "demo-7",
            name = "Aditi Rao",
            bio = "Data scientist passionate about predictive modeling, tabular transformers, and statistical analysis.",
            skills = listOf("Data Science", "Python", "Machine Learning"),
            interests = listOf("Healthcare", "Climate Tech"),
            experience = "Intermediate",
            available = true
        ),
        Profile(
            id = "demo-8",
            name = "Vikram Sengupta",
            bio = "Robotics and autonomous systems developer with hands-on ROS and LiDAR obstacle avoidance experience.",
            skills = listOf("Robotics", "C/C++", "Computer Vision"),
            interests = listOf("Autonomous Drones", "Sensor Fusion"),
            experience = "Advanced",
            available = true
        ),
        Profile(
            id = "demo-9",
            name = "Sneha Kulkarni",
            bio = "Cloud native developer building serverless event architectures and fast mobile backend APIs.",
            skills = listOf("Backend", "Node.js", "Firebase", "GraphQL"),
            interests = listOf("Microservices", "Developer Tools"),
            experience = "Intermediate",
            available = true
        ),
        Profile(
            id = "demo-10",
            name = "Arjun Nair",
            bio = "Full-stack mobile engineer building Bluetooth Low Energy diagnostics and peripheral communication apps.",
            skills = listOf("Android", "Kotlin", "Bluetooth", "IoT"),
            interests = listOf("Wearables", "Health Tech"),
            experience = "Beginner",
            available = true
        )
    )

    val projects: List<Project> = listOf(
        Project(
            id = "proj-1",
            title = "Smart Traffic AI",
            description = "Real-time intersection monitoring using edge computer vision cameras to optimize green light cycles dynamically without cloud latency.",
            requiredSkills = listOf("Computer Vision", "Edge AI", "Embedded Systems", "Python"),
            memberIds = listOf("demo-1", "demo-2")
        ),
        Project(
            id = "proj-2",
            title = "Campus Health Tracker",
            description = "Privacy-first offline biometric sync for students and athletes using Bluetooth beacons and low-power wearable telemetry.",
            requiredSkills = listOf("Android", "IoT", "UI/UX", "Bluetooth"),
            memberIds = listOf("demo-10", "demo-3")
        ),
        Project(
            id = "proj-3",
            title = "Offline Mesh Chat",
            description = "Disaster-resilient ad-hoc communication network transmitting encrypted voice packets and SOS signals over Wi-Fi Direct and BLE.",
            requiredSkills = listOf("Mesh Networks", "Cybersecurity", "Android", "Cryptography"),
            memberIds = listOf("demo-6", "demo-1")
        )
    )

    val tasks: Map<String, List<ProjectTask>> = mapOf(
        "proj-1" to listOf(
            ProjectTask("t1-1", "proj-1", "Calibrate camera distortion parameters for wide-angle lens", true),
            ProjectTask("t1-2", "proj-1", "Deploy quantized YOLOv8 model to Edge AI accelerator", true),
            ProjectTask("t1-3", "proj-1", "Implement GPIO trigger interface for traffic light relay", false),
            ProjectTask("t1-4", "proj-1", "Test night-time vehicle detection robustness in rain", false)
        ),
        "proj-2" to listOf(
            ProjectTask("t2-1", "proj-2", "Define GATT service UUIDs and characteristic schemas", true),
            ProjectTask("t2-2", "proj-2", "Design dark-mode telemetry dashboard in Figma", true),
            ProjectTask("t2-3", "proj-2", "Build background sync service for heart-rate anomaly alert", false)
        ),
        "proj-3" to listOf(
            ProjectTask("t3-1", "proj-3", "Implement Curve25519 key exchange over Nearby Connections", true),
            ProjectTask("t3-2", "proj-3", "Build store-and-forward packet caching in local Room database", false),
            ProjectTask("t3-3", "proj-3", "Verify multihop packet routing resilience under 50% node drop", false)
        )
    )
}
