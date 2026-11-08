package com.pianomaster.app.domain

object LessonCatalog {
    val lessons = listOf(
        Lesson("L01", "Find the C notes", "Beginner", listOf(
            LessonStep("1", "Find a group of two black keys. The white key immediately to its left is C.", 60, "Middle C is MIDI note 60."),
            LessonStep("2", "Play Middle C once and listen to its pitch.", 60, "Use a relaxed thumb."),
            LessonStep("3", "Find another C elsewhere on your keyboard.", 72, "C repeats every 12 semitones."),
            LessonStep("4", "Say the note names C-D-E-F-G-A-B in order.", null, "After B, the sequence starts again at C.")
        )),
        Lesson("L02", "Five-finger position", "Beginner", listOf(
            LessonStep("1", "Place your right thumb on C.", 60, "Keep your wrist relaxed."),
            LessonStep("2", "Play C-D-E-F-G with fingers 1-2-3-4-5.", null, "Play each note evenly."),
            LessonStep("3", "Return G-F-E-D-C.", null, "Avoid lifting your hand too high."),
            LessonStep("4", "Repeat the pattern slowly five times.", null, "Aim for even volume and timing.")
        )),
        Lesson("L03", "Read simple note patterns", "Beginner", listOf(
            LessonStep("1", "Find D, the white key between the two black keys.", 62, "D is between C and E."),
            LessonStep("2", "Find E, the white key to the right of the pair of black keys.", 64, "E is immediately after D."),
            LessonStep("3", "Play C-D-E, then E-D-C.", null, "Listen for the melody moving up and down.")
        )),
        Lesson("L04", "Your first major scale", "Beginner", listOf(
            LessonStep("1", "Play C-D-E-F-G-A-B-C.", 60, "C major uses only white keys."),
            LessonStep("2", "Play the scale backwards: C-B-A-G-F-E-D-C.", 72, "Keep a steady pulse."),
            LessonStep("3", "Notice the whole-step and half-step pattern.", null, "Major scale pattern: whole, whole, half, whole, whole, whole, half.")
        )),
        Lesson("L05", "Build your first chords", "Beginner", listOf(
            LessonStep("1", "Play C, E and G together to make C major.", 60, "A major triad uses scale degrees 1, 3 and 5."),
            LessonStep("2", "Find F-A-C for F major.", 65, "Use a relaxed hand shape."),
            LessonStep("3", "Find G-B-D for G major.", 67, "Play the notes together gently.")
        )),
        Lesson("L06", "Play your first melody", "Beginner", listOf(
            LessonStep("1", "Play C-C-G-G-A-A-G.", 60, "This is the opening phrase of Twinkle Twinkle Little Star."),
            LessonStep("2", "Play F-F-E-E-D-D-C.", 65, "Keep the rhythm even."),
            LessonStep("3", "Join both phrases and repeat slowly.", null, "Pause briefly between phrases.")
        ))
    )
}
