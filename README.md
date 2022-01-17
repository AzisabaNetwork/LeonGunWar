[![Build Test](https://github.com/AzisabaNetwork/LeonGunWar/actions/workflows/build-test.yml/badge.svg)](https://github.com/AzisabaNetwork/LeonGunWar/actions/workflows/build-test.yml)
[![](https://badgen.net/twitter/follow/AzisabaNetwork/?icon=twitter)](https://twitter.com/AzisabaNetwork)
[![](https://discordapp.com/api/guilds/357134045328572418/widget.png)](https://discord.gg/azisaba)

# LeonGunWar plugin
このプラグインは [アジ鯖](https://azisaba.net) 内にあるLGWサーバーのコアとなるPluginです

## 説明
現在アジ鯖では多くのプレイヤーに遊んでいただき、特にプレイヤーがLGWに集中しています。</br>
そのため多くの要望や改善点が寄せられていますが、現在のLGWはほぼ配布Pluginで動いているため新機能、特に試合系の機能追加が難しい状態となっています。</br>
このPluginは前述した問題を解決するために作成されています。

## 機能
* 試合の管理
* (その他 非公開機能)

## ビルド前に
```sh
#!/bin/bash

# WorldEdit v6.1.9をローカルのMavenRepoにインストール
curl -L -o WorldEdit.jar https://dev.bukkit.org/projects/worldedit/files/2597538/download
mvn install:install-file -Dfile=./WorldEdit.jar -DgroupId=com.sk89q -DartifactId=worldedit -Dversion=6.1.9 -Dpackaging=jar -DgeneratePom=true

# WorldGuard v6.2.2をローカルのMavenRepoにインストール
curl -L -o WorldGuard.jar https://dev.bukkit.org/projects/worldguard/files/2610618/download
mvn install:install-file -Dfile=./WorldGuard.jar -DgroupId=com.sk89q -DartifactId=worldguard -Dversion=6.2.2 -Dpackaging=jar -DgeneratePom=true

# CrackShot v0.98.11をローカルのMavenRepoにインストール
curl -L -o CrackShot.jar https://dev.bukkit.org/projects/crackshot/files/3151915/download
mvn install:install-file -Dfile=./CrackShot.jar -DgroupId=com.shampaggon -DartifactId=CrackShot -Dversion=0.98.11 -Dpackaging=jar -DgeneratePom=true

# BuildToolsで1.12.2 Spigotをビルド
mkdir -p ./build && curl -L -o ./build/BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
cd build && java -jar BuildTools.jar --rev 1.12.2
```

## コミットのPrefix
|Prefix   |内容     |
|---------|--------|
|[Add]    |ファイル追加 / 機能追加|
|[Delete] | ファイル削除 / 機能削除|
|[Update] | 機能修正 (バグ修正を除く)|
|[Fix]    |バグ修正|
|[HotFix] |クリティカルなバグ修正|
|[Clean]  |リファクタリング / コード整理|
|[Change] | 仕様変更|
|[Rename] | 名前変更|
|[Docs] | ドキュメント(説明)系の編集|
|[Debug] | デバッグコードに関する編集 |

## Contributors
<a href="https://github.com/AzisabaNetwork/LeonGunWar/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=AzisabaNetwork/LeonGunWar" />
</a>
