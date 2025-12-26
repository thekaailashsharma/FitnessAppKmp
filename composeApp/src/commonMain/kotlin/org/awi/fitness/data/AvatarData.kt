package org.awi.fitness.data

import org.awi.fitness.model.AvatarSelection

object AvatarData {
    val avatars = listOf(
        AvatarSelection(
            id = "avatar_1",
            name = "Zen Master",
            imageUrl = "avatar1", // Resource name without extension
            isSelected = false
        ),
        AvatarSelection(
            id = "avatar_2",
            name = "Energy Spark",
            imageUrl = "avatar2",
            isSelected = false
        ),
        AvatarSelection(
            id = "avatar_3",
            name = "Iron Will",
            imageUrl = "avatar3",
            isSelected = false
        ),
        AvatarSelection(
            id = "avatar_4",
            name = "Calm Flow",
            imageUrl = "avatar4",
            isSelected = false
        ),
        AvatarSelection(
            id = "avatar_5",
            name = "Speed Racer",
            imageUrl = "avatar5",
            isSelected = false
        ),
        AvatarSelection(
            id = "avatar_6",
            name = "Mountain Mover",
            imageUrl = "avatar6",
            isSelected = false
        )
    )
    
    // Background resources
    val backgrounds = listOf("background1", "background2", "background3")
}

