# senior-center-twerk-machine
경로당 트월킹 머신

## 사용법
`/t[werk] <owner of skin>`

![사진](images/image.gif)

## 작동원리

tap관련 설명은 제외하였습니다. 자세한건 [Monun Docs](https://monun.me/dev/tap/fake-entity)에서 확인해주세요   
물론 FakePlayer api는 살짝 바뀌었지만 그러려니 하고 넘어가주십시길 바랍니다


설명에 앞서 '경로당 트월킹 머신'이란 단어가 길어 '경트머'로 부르도록 하겠습니다


경트머에 커맨드 twerk에서 유저이름을 받습니다   
NPC를 생성하기전 탈것이 필요합니다   
경트머의 NPC 자세를 확인하면 알겠지만 앉아있는 자세입니다   
그렇기에 투명 아머스탠드 생성후   
NPC를 twerk에서 받은 유저이름으로 스킨을 가져와서 만들어   
그 NPC를 아머스탠드에 탑승시킵니다   


그리고 여기서 앉기만 하면 그저 앉아있기만 하여   
자세를 숙일수가 없게 됩니다   
그래서 NPC의 상태를 겉날개로 나는 상태를 만듭니다   
그렇게 하여 NPC의 시선에 따라서 위 이미지처럼 움직이게 됩니다   
![Npc Rotating Image](https://i.imgur.com/qmvJmyU.gif)   
자세한것은 [여기서](https://www.spigotmc.org/threads/packet-discovery-rotating-player-models.318388/#post-3024113) 참조 하십시는걸 추천드립니다


혹시나 따라만들사람~~없겠지만~~이 있다면 알려드려야 될것이 있다면
Tap상에서는 Passenger 상태에서는 바라보는 시점을 바꾸는것을 FakeEntity 내에서 하는것
많은 시도를 해봤지만 결국 안되는것 같아 패킷으로 처리하였습니다


그렇게 하여 NPC는 앉아있는 상태임과 동시에   
시선에 따라서 몸을 회전할수있는 상태가 됩니다   
어케 찾았냐고요? ~~몰라요 그냥 놀다가 찾았어요~~


사실 이걸 1년 전쯤에 찾았지만 만들기 귀찮아서 그동안 안했습니다 ㅋㅋ
