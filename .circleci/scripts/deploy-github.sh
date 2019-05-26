#! /bin/bash

export CIRCLE_PARENT_BUILD_NUM="$(cat CIRCLE_PARENT_BUILD_NUM)"

USERNAME="$CIRCLE_PROJECT_USERNAME"
REPO="$CIRCLE_PROJECT_REPONAME"
COMMIT="$CIRCLE_SHA1"
TITLE="$CIRCLE_PROJECT_REPONAME #$CIRCLE_PARENT_BUILD_NUM"
BODY="$(dirname $CIRCLE_BUILD_URL)/$CIRCLE_PARENT_BUILD_NUM"

TAG="#$CIRCLE_PARENT_BUILD_NUM"

ghr -u "$USERNAME" -r "$REPO" -c "$COMMIT" -n "$TITLE" -b "$BODY" "$TAG" artifacts/
