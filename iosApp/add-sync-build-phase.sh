#!/bin/bash

PROJECT_FILE="iosApp.xcodeproj/project.pbxproj"
SYNC_PHASE_ID="A1B2C3D4E5F6789A12345678"

echo "🔧 Adding sync-env build phase to Xcode project..."

# Check if the sync phase already exists
if grep -q "$SYNC_PHASE_ID" "$PROJECT_FILE"; then
    echo "✅ Sync build phase already exists!"
    exit 0
fi

# Create a backup
cp "$PROJECT_FILE" "${PROJECT_FILE}.backup"

# Create temporary files
TEMP_FILE=$(mktemp)
TEMP_FILE2=$(mktemp)

# Step 1: Add the phase ID to the buildPhases array
# Find the line with "9732983FEE7401A8A2E53DAF /* [CP] Check Pods Manifest.lock */," and add our phase after it
awk '
/9732983FEE7401A8A2E53DAF \/\* \[CP\] Check Pods Manifest\.lock \*\/,/ {
    print $0
    if (/,/) {
        print "\t\t\t\tA1B2C3D4E5F6789A12345678 /* Sync Environment Config */,"
    }
    next
}
{print}
' "$PROJECT_FILE" > "$TEMP_FILE"

# Step 2: Add the shell script build phase definition
# Find the end of PBXShellScriptBuildPhase section and add our phase before it
awk '
/\/\* End PBXShellScriptBuildPhase section \*\// {
    print "\t\tA1B2C3D4E5F6789A12345678 /* Sync Environment Config */ = {"
    print "\t\t\tisa = PBXShellScriptBuildPhase;"
    print "\t\t\tbuildActionMask = 2147483647;"
    print "\t\t\tfiles = ("
    print "\t\t\t);"
    print "\t\t\tinputFileListPaths = ("
    print "\t\t\t);"
    print "\t\t\tname = \"Sync Environment Config\";"
    print "\t\t\toutputFileListPaths = ("
    print "\t\t\t);"
    print "\t\t\trunOnlyForDeploymentPostprocessing = 0;"
    print "\t\t\tshellPath = /bin/sh;"
    print "\t\t\tshellScript = \"./sync-env.sh\\n\";"
    print "\t\t\tshowEnvVarsInLog = 0;"
    print "\t\t};"
}
{print}
' "$TEMP_FILE" > "$TEMP_FILE2"

# Replace the original file
mv "$TEMP_FILE2" "$PROJECT_FILE"

# Clean up
rm -f "$TEMP_FILE" "$TEMP_FILE2"

# Verify the project file is still valid
if plutil -lint "$PROJECT_FILE" > /dev/null 2>&1; then
    echo "✅ Successfully added sync build phase to Xcode project!"
    echo "📝 The sync-env.sh script will now run automatically before each build."
    echo "🧹 Backup saved as ${PROJECT_FILE}.backup"
else
    echo "❌ Error: Project file became invalid. Restoring backup..."
    mv "${PROJECT_FILE}.backup" "$PROJECT_FILE"
    exit 1
fi 