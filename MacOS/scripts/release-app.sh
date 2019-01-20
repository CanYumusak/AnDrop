#!/bin/bash

rm -rf "release/*"

xcodebuild \
	-scheme "AndroidDrop" \
	-archivePath "build/AndroidDrop.xcarchive" \
	-configuration Release \
	archive 

xcodebuild \
	-target AndroidDrop \
	-project AndroidDrop.xcodeproj \
	-configuration release \
	-exportArchive \
	-exportOptionsPlist "scripts/Info.plist" \
	-archivePath "build/AndroidDrop.xcarchive" \
	-showdestinations \
	-exportPath "build/release/" \
	-showBuildSettings

create-dmg \
	--volname "Androp Installer" \
	--window-pos 200 120 \
	--window-size 800 400 \
	--icon-size 100 \
	--icon "Application.app" 200 190 \
	--hide-extension "Application.app" \
	--app-drop-link 600 185 \
	"release/Androp-Installer.dmg" \
	"build/release/"
