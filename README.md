# mojimoji プロジェクト

> **GPT** を利用してストーリーを生成し、**日本語漢字** を学習し、**クイズ** や **コレクション** を楽しむことができる **ウェブアプリケーション** です。

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](#)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Version](https://img.shields.io/badge/version-1.0.0-blue)](#)
[![Contributors](https://img.shields.io/github/contributors/SCITSanKumi/mojimoji)](#)

---

## 目次

- [プロジェクト概要](#プロジェクト概要)
- [主な機能](#主な機能)
  - [1) GPTストーリー生成](#1-gptストーリー生成)
  - [2) 漢字クイズ & コレクション](#2-漢字クイズ--コレクション)
  - [3) ストーリー共有 & 掲示板](#3-ストーリー共有--掲示板)
  - [4) ランキングシステム](#4-ランキングシステム)
  - [5) マイページ & 統計](#5-マイページ--統計)
- [紹介動画](#紹介動画)
- [技術スタック](#技術スタック)
- [ライセンス](#ライセンス)
- [貢献者 (Contributors)](#貢献者-contributors)

---

## プロジェクト概要

**mojimoji** は、ユーザーが入力したキーワードや状況に基づいて GPT がストーリーを生成し、そのストーリー内に自然に挿入された **日本語漢字** を学習できる統合ウェブアプリケーションです。  
本アプリケーションは、フロントエンドとバックエンドが統合され、一つのアプリケーションとして展開され、以下の主要な目標を持っています：

- **ストーリー生成**: ユーザー入力に基づいて GPT が創造的なストーリーを自動展開します。
- **漢字学習およびクイズ**: ストーリー内の漢字を活用したクイズを通じて音と意味を学び、正答処理された漢字はカード形式で収集されます。
- **コミュニティおよび共有**: ユーザーは生成されたストーリーを掲示板で共有し、他のユーザーとコメントや推薦機能を通じて交流することができます。
- **ランキングおよび統計**: 様々な活動指標に基づいてランキングシステムを提供し、マイページで個人学習の統計を確認することができます。

---

## 主な機能

### 1) GPTストーリー生成

- **キーワード/状況入力**: ユーザーが望むテーマ、キーワード、ジャンルなどを簡単に入力します。
- **AIストーリー展開**: GPT が入力値に基づいてシナリオ、セリフ、背景説明などを含むストーリーを自動生成します。
- **漢字挿入**: 生成されたストーリー内に自然に **日本語漢字** が含まれており、学習を促します。

### 2) 漢字クイズ & コレクション

- **クイズ解答**: ストーリーに登場する漢字を基に、音と意味のクイズに解答して得点を獲得します。
- **コレクションカード**: 正解処理された漢字はカード形式で収集され、自分だけの漢字収集帳を構成することができます。
- **カード詳細情報**: 各カードには漢字の **JLPTレベル**、**音読み/訓読み**、**意味**、**例単語** などが含まれます。

### 3) ストーリー共有 & 掲示板

- **ストーリー投稿**: ユーザーが生成したストーリーを基に、サムネイルとタイトルを AI が生成し、掲示板にアップロードして他のユーザーと共有します。
- **コメントおよび推薦**: ストーリーにコメントを残したり、いいね/推薦機能を通じてユーザー間の交流を促進します。

### 4) ランキングシステム

- **総合ランキング**: 誰が最も多くの総合点を収集したかを確認できます。
- **漢字収集ランキング**: 誰が最も多くの漢字を収集したかを確認できます。
- **ストーリーランキング**: 作成したストーリーの数と推薦数に基づいて順位が算出されます。
- **総合活動ランキング**: 様々な活動指標を総合して全体順位を提供します。

### 5) マイページ & 統計

- **プロフィール管理**: ニックネーム、メールアドレス、パスワードなどの個人情報を修正できます。
- **学習統計**: 収集した漢字の数、JLPTレベル別の達成度、最近のクイズ得点推移、日/週ごとの漢字獲得量など、個人に合わせた統計情報を提供します。

---

## 紹介動画

[![mojimoji 紹介動画](https://img.youtube.com/vi/pi9H5oWwjZQ/0.jpg)](https://youtu.be/pi9H5oWwjZQ)

---

## 技術スタック

| 分野                   | 使用技術                                             |
| ---------------------- | ------------------------------------------------------ |
| **Frontend/Backend**   | **Java 17**, Spring Boot, Thymeleaf, HTML5, CSS3, JavaScript, Bootstrap |
| **Database**           | MySQL, H2                                       |
| **Infra**              | AWS EC2/RDS/NGINX、ローカル環境                          |
| **ライブラリ & API**   | GSAP, anime.js, VanillaTilt, jQuery, OpenAI GPT, OAuth2.0 など      |

---

## ライセンス

本プロジェクトは MIT License に従います。

## 貢献者 (Contributors)

- [@RealHatawa](https://github.com/RealHatawa)
- [@mk0131](https://github.com/mk0131)
- [@yangyeahhhhh](https://github.com/yangyeahhhhh)
- [@yesini](https://github.com/yesini)
- [@cc1785](https://github.com/cc1785)
