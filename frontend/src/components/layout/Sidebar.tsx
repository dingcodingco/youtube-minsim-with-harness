export default function Sidebar() {
  const navItems = [
    { label: '대시보드', icon: '📊', active: true },
    { label: '영상 관리', icon: '🎬', active: false },
    { label: '설정', icon: '⚙️', active: false },
  ];

  return (
    <aside className="hidden w-56 border-r border-slate-700 bg-pulse-card lg:block">
      <nav className="flex flex-col gap-1 p-4">
        {navItems.map((item) => (
          <button
            key={item.label}
            className={`flex items-center gap-3 rounded-lg px-3 py-2 text-left text-sm transition-colors ${
              item.active
                ? 'bg-slate-700 text-pulse-text'
                : 'text-slate-400 hover:bg-slate-700/50 hover:text-slate-200'
            }`}
            disabled={!item.active}
          >
            <span role="img" aria-hidden="true">
              {item.icon}
            </span>
            {item.label}
          </button>
        ))}
      </nav>
    </aside>
  );
}
