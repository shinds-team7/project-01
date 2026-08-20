(() => {
  // 구형 홈에서 쓰던 목업 데이터. 화면 렌더링에는 쓰지 않으며, 실제 장소 데이터와의
  // 디자인 비교·개발용 참고값으로만 남겨 둔다.
  const LEGACY_MOCK_HOSTS = Object.freeze([
    {name: '햇살 가득한 마당', region: '마포구 연남동', price: 5000, availableToday: true},
    {name: '성수의 조용한 오후', region: '성동구 성수동', price: 7000, availableToday: true},
    {name: '조용한 망원 하루', region: '마포구 망원동', price: 4000, availableToday: true},
    {name: '구름이네 캣 스테이', region: '서초구 서초동', price: 5500, availableToday: false}
  ]);
  const form = document.querySelector('[data-home-search]');
  if (!form) return;
  const input = form.elements.query;
  const container = document.querySelector('[data-recent-searches]');
  const endpoint = '/api/home/recent-searches';
  const render = (queries) => {
    if (!queries?.length) return;
    container.replaceChildren(...queries.map((query) => {
      const button = document.createElement('button');
      button.type = 'button'; button.textContent = query;
      button.addEventListener('click', () => { input.value = query; form.requestSubmit(); });
      return button;
    }));
    container.hidden = false;
  };
  fetch(endpoint, {headers: {Accept: 'application/json'}}).then(r => r.ok ? r.json() : []).then(render).catch(() => {});
  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const query = input.value.trim();
    if (!query) { input.focus(); return; }
    try {
      const response = await fetch(endpoint, {method: 'POST', headers: {'Content-Type': 'application/json', Accept: 'application/json'}, body: JSON.stringify({query})});
      if (response.ok) render(await response.json());
    } catch (_) { /* 검색어 저장 실패는 장소 탐색을 막지 않는다. */ }
    window.location.assign(`/search?keyword=${encodeURIComponent(query)}`);
  });
})();
