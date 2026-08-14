package scripts.`create-module`

plugins {
    id ("minirogue.multiplatform.library")
}

minirogue {
    platforms {
        android()
        jvm()
    }
}

kotlin{
    sourceSets{
        commonMain{
            dependencies {
                api(PUBLIC_PLACEHOLDER)
            }
        }
    }
}

