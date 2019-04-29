#!/bin/bash

set -e

GIT_BIN="${GIT_BIN:-git}"

echo "Project version is [${PROJECT_VERSION}]"
if [[ "${PROJECT_VERSION}" != *"RELEASE"* ]];then
    echo "Will not update guides, since the version is not a RELEASE one"
    exit 0
fi

# The script should be executed from the root folder
[[ -z "${ROOT_FOLDER}" ]] && ROOT_FOLDER="$( pwd )"
echo "Current folder is ${ROOT_FOLDER}"

if [[ ! -e "${ROOT_FOLDER}/.git" ]]; then
    echo "You're not in the root folder of the project!"
    exit 1
fi

GUIDES_FOLDER="${ROOT_FOLDER}/guides"
SPRING_GUIDES_REPO_ROOT="${SPRING_GUIDES_REPO_ROOT:-git@github.com:spring-guides}"
echo "The Spring Guides repo root is [${SPRING_GUIDES_REPO_ROOT}]"

if [[ ! -d "${GUIDES_FOLDER}" ]]; then
  echo "No [guides] folder is present. Won't do anything"
  exit 0
fi

function iterate_over_guides() {
    for dir in "${GUIDES_FOLDER}"/*/     # list directories in the form "/tmp/dirname/"
    do
        local dir="${dir%*/}"      # remove the trailing "/"
        local folderName="${dir##*/}"    # print everything after the final "/"
        if [[ "${folderName}" == "target" ]]; then
            echo "Skipping [${folderName}]"
            continue
        fi
        local guideRepo="${SPRING_GUIDES_REPO_ROOT}/${folderName}"
        local clonedGuideFolderParent="${ROOT_FOLDER}/target"
        local clonedGuideFolder="${clonedGuideFolderParent}/${folderName}"
        echo "Will clone [${guideRepo}] project [${folderName}] to target"
        mkdir -p "${clonedGuideFolder}"
        rm -rf "${clonedGuideFolder}"
        "${GIT_BIN}" clone "${guideRepo}" "${clonedGuideFolder}"
        pushd "${clonedGuideFolder}"
            echo "Will start the commit process for [${folderName}]"
            add_oauth_token_to_remote_url
            remove_all_files
            copy_new_guide "${GUIDES_FOLDER}/${folderName}" "${clonedGuideFolderParent}"
            commit_and_push_new_guide_contents
            echo "Successfully copied and pushed new guides for [${guideRepo}]"
        popd
    done
}

function commit_and_push_new_guide_contents() {
    "${GIT_BIN}" add .
    "${GIT_BIN}" commit -m "Updating guides" || echo "Nothing to commit"
    "${GIT_BIN}" push origin master || echo "Nothing to push"
}

function copy_new_guide() {
    local guideFolder="${1}"
    local clonedGuideFolder="${2}"
    echo "Will copy new guide files from [${guideFolder}/] to [${clonedGuideFolder}]"
    cp -r "${guideFolder}/" "${clonedGuideFolder}/"
    echo "Copied new guide files"
}

function remove_all_files() {
    echo "Removing all non git files in [$( pwd )]"
    "${GIT_BIN}" rm -rf "$( pwd )/"
    "${GIT_BIN}" clean -fxd
    echo "All files removed"
}

# Adds the oauth token if present to the remote url
function add_oauth_token_to_remote_url() {
    local remote
    remote="$( "${GIT_BIN}" config remote.origin.url | sed -e 's/^git:/https:/' )"
    echo "Current remote [${remote}]"
    if [[ "${RELEASER_GIT_OAUTH_TOKEN}" != "" && ${remote} != *"@"* ]]; then
        echo "OAuth token found. Will reuse it to push the code"
        withToken=${remote/https:\/\//https://${RELEASER_GIT_OAUTH_TOKEN}@}
        "${GIT_BIN}" remote set-url --push origin "${withToken}"
    else
        echo "No OAuth token found"
        "${GIT_BIN}" remote set-url --push origin "$( "${GIT_BIN}" config remote.origin.url | sed -e 's/^git:/https:/' )"
    fi
}

# Prints the usage
function print_usage() {
cat <<EOF
The idea of this script is to keep the Spring guides up to date by taking the
contents of guides from the project's repository and pushing them to Spring guides
repository.

This script iterates over the contents of the [guides] folder. Each its subfolder
corresponds to a repository under the Spring Guides repository (i.e. the https://github.com/spring-guides organization).

Upon the iteration a corresponding guide repository gets cloned and updated with the latest content from the project repository. A commit and push then takes place.

USAGE:

You can use the following options:

-h|--help           - display this message

EOF
}


# ==========================================
#    ____   ____ _____  _____ _____ _______
#  / ____|/ ____|  __ \|_   _|  __ \__   __|
# | (___ | |    | |__) | | | | |__) | | |
#  \___ \| |    |  _  /  | | |  ___/  | |
#  ____) | |____| | \ \ _| |_| |      | |
# |_____/ \_____|_|  \_\_____|_|      |_|
#
# ==========================================


if [[ "${SOURCE_FUNCTIONS}" == "true" ]]; then
    echo "Will just source functions. Will not run any commands"
else
    while [[ $# -gt 0 ]]
    do
    key="$1"
    case "${key}" in
        -h|--help)
        print_usage
        exit 0
        ;;
        *)
        echo "Invalid option: [$1]"
        print_usage
        exit 1
        ;;
    esac
    shift # past argument or value
    done

    iterate_over_guides
fi

