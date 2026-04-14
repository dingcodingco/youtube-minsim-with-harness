import type { ReactNode } from 'react';

interface HeaderProps {
  children?: ReactNode;
}

export default function Header({ children }: HeaderProps) {
  return (
    <header className="sticky top-0 z-50 border-b border-slate-700 bg-[#0f172a]/95 backdrop-blur-sm">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3 sm:px-6">
        <div className="flex items-center gap-3">
          <h1 className="text-xl font-bold text-pulse-text sm:text-2xl">
            YooBe Pulse
          </h1>
          <span className="text-lg" role="img" aria-label="target">
            🎯
          </span>
        </div>
        {children}
      </div>
    </header>
  );
}
