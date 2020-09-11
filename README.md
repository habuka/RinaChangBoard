# 璃奈ちゃんボード
## 概要
「ラブライブ！虹ヶ咲学園スクールアイドル同好会」の登場人物「天王寺璃奈」ちゃんが装着している  
「オートエモーションコンバート璃奈ちゃんボード」をAndroidアプリで再現したものです。  
音声認識によって表情を8通りに切り替えることができます。

## 導入方法
本リポジトリをcloneまたはダウンロードし、Android Studioでビルドしてお手持ちのAndroid端末に書き込むことで動作します。  

(Androidの開発環境を導入済の方は1,2は飛ばしてください)
1. AndroidのUSBデバッグを有効にします。(https://developer.android.com/studio/debug/dev-options?hl=ja)
1. Android
1. 本リポジトリをcloneまたはzipでダウンロードします。
1. Android Studioを開き、「プロジェクトのインポート」から本プロジェクトをインポートします。
1. お手持ちのAndroid端末をPCに接続し、「実行」をクリックすることでビルドが開始され、Android端末に書き込まれます。

## 使い方
アプリを起動すると、最初にメニュー画面が表示されます。
- メインモード  
通常のモードです。基本的にはこちらを使用してください。
- デバッグモード(認識単語表示)  
画面中央に認識した単語が表示されるようになります。自分で表情を追加したい時に使用してください。

どちらかのモードを起動すると、画面に表情が表示されます。  
表情が表示されている状態で「璃奈ちゃんボード」と発声すると、表情コマンド待受モードになります。(表情が消えます)  
表情コマンド待受モードで表情コマンドを発声すると、対応した表情に切り替わります。  
現在実装している表情コマンドは以下の8つです。
- ノーマル
- にっこりん
- むんっ
- しょんぼり
- ぷんぷん
- びっくり
- ウインク
- リラックス

再度表情を切り替えたい場合は、同様にして「璃奈ちゃんボード」と発声して表情コマンド受付モードに移行させた後に、
表情コマンドを発声してください。  

## 表情を追加したい方向け
表情コマンドおよび表情ステートの追加と、対応する表情の画像を登録することで、オリジナルの表情を表示させることもできます。
- `SimpleModeActivity.java`の`FACE_XX`に既存のものと重複しないように任意の表情番号を追加
- `SimpleModeActivity.java`の`searchFace()`に表情コマンド検索処理を追加  
(既存のものを参考にしてください。デバッグモードでその単語がどのように認識されるかを確認して、少しゆるめに候補を用意しておくと良いです)
- res/drawableに表情の画像を追加してください。

## その他
- とりあえず動けばヨシ！で作ったので不安定かもしれません。ご了承ください
- タブレットでやるとそれっぽくなります
- マイコンとBluetoothで連携して実際のLEDを光らせる機能(https://twitter.com/HakubutsukanP/status/1233918853157015553)  
もいちおう公開準備を進めています。

