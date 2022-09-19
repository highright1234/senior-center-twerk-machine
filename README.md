# 경로당 트월킹 머신
## 모드를 쓰지 않은 프로젝트입니다
## 설명
그냥 할거없어서 만든 작은 Toy project 입니다

## 사용법
`/t[werk] <owner of skin>`   
현재 개발자가 귀찮음으로 인해
리로드가 아니면 NPC를 삭제할수 없다는점...

![사진](images/image.gif)
---
## 작동원리

tap관련 설명은 제외하였습니다. 자세한건 [Monun Docs](https://monun.me/dev/tap/fake-entity)에서 확인해주세요   
물론 FakePlayer api는 살짝 바뀌었지만 그러려니 하고 넘어가주십시길 바랍니다


설명에 앞서 '경로당 트월킹 머신'이란 단어가 길어 '경트머'로 부르도록 하겠습니다


경트머에 커맨드 twerk에서 유저이름을 받습니다   
NPC를 생성하기전 탈것이 필요합니다   
경트머의 NPC 자세를 확인하면 알겠지만 앉아있는 자세입니다   
그렇기에 투명 아머스탠드 생성후   
NPC를 twerk에서 받은 유저이름의 스킨을딴 NPC를 만들어   
그 NPC를 아머스탠드에 탑승시킵니다   


그리고 앉기만 하면 당연히 자세를 숙일수가 없게 됩니다   
그러니 숙일수가 있게 NPC의 상태를 겉날개로 나는 상태를 만듭니다   
그렇게 하면 NPC의 시선에 따라서 위 이미지처럼 움직일수 있게 됩니다   
![Npc Rotating Image](https://i.imgur.com/qmvJmyU.gif)   
자세한것은 [여기서](https://www.spigotmc.org/threads/packet-discovery-rotating-player-models.318388/#post-3024113) 참조 하십시는걸 추천드립니다


혹시나 따라만들사람~~없겠지만~~이 있다면 알려드려야 될것이 있다면   
Tap상에서는 Passenger 상태에서는 바라보는 시점을 바꾸는것을 FakeEntity 내에서 하는것   
많은 시도를 해봤지만 결국 안되는것 같아 패킷으로 처리하였습니다   


그렇게 하여 NPC는 앉아있는 상태임과 동시에   
시선에 따라서 몸을 회전할수있는 상태가 됩니다   
어케 찾았냐고요? ~~몰라요 그냥 놀다가 찾았어요~~


사실 이걸 1년 전쯤에 찾았지만 만들기 귀찮아서 그동안 안했습니다 ㅋㅋ   
그리고 twerk가 명사로도 트월킹인줄 알았지만 다른뜻이였다는   
하지만 귀찮아서 놔뒀습니다   

## Used Libraries
> Coroutine   
> [MCCoroutine](https://github.com/Shynixn/MCCoroutine)   
> [Komamnd](https://github.com/monun/kommand)   
> [InvFX](https://github.com/monun/invfx)   
> [Tap](https://github.com/monun/tap)   

