#-dontpreverify
-dontoptimize
-dontshrink
# Obfuscation breaks coroutines/ktor for some reason
-dontobfuscate

# Todo: this should help us create the rules, but it does not build
#-addconfigurationdebugging