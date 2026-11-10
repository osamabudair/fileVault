import { useFiles } from '../../context/FileContext';
import { useAuth } from '../../context/AuthContext';
import { 
  Vault, 
  Files, 
  Users, 
  Star, 
  Trash2, 
  LogOut, 
  HardDrive 
} from 'lucide-react';

export const Sidebar = ({ className = '' }) => {
  const { activeTab, setActiveTab, counts } = useFiles();
  const { user, logout } = useAuth();

  const navItems = [
    { id: 'all', label: 'All Files', icon: Files, count: counts.all },
    { id: 'shared', label: 'Shared', icon: Users, count: counts.shared },
    { id: 'favorites', label: 'Favorites', icon: Star, count: counts.favorites },
    { id: 'trash', label: 'Trash', icon: Trash2, count: counts.trash }
  ];

  return (
    <aside className={`w-64 h-screen bg-white/75 dark:bg-[#181B20]/70 backdrop-blur-md border-r border-white/60 dark:border-white/[0.08] shadow-[0_4px_20px_rgba(0,0,0,0.02)] flex flex-col justify-between select-none p-4 transition-colors duration-150 ${className}`}>
      {/* Top: Logo & Navigation */}
      <div className="space-y-6">
        {/* Workspace Logo */}
        <div className="flex items-center gap-2.5 px-2 pt-1">
          <div className="w-8 h-8 rounded-xl bg-black/[0.04] dark:bg-white/[0.06] border border-black/[0.06] dark:border-white/[0.08] flex items-center justify-center text-zinc-800 dark:text-zinc-200">
            <Vault className="w-4.5 h-4.5 text-[#721C24] dark:text-[#9F2D3B]" />
          </div>
          <div>
            <span className="font-semibold text-sm tracking-tight text-zinc-900 dark:text-zinc-100">
              FileVault
            </span>
            <p className="text-[10px] text-zinc-500 dark:text-zinc-400">Personal Cloud</p>
          </div>
        </div>

        {/* Navigation links */}
        <div className="space-y-1">
          <p className="px-2 text-[10px] font-semibold uppercase tracking-wider text-zinc-400 dark:text-zinc-500">
            Files
          </p>

          <nav className="space-y-0.5 pt-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = activeTab === item.id;

              return (
                <button
                  key={item.id}
                  onClick={() => setActiveTab(item.id)}
                  className={`w-full flex items-center justify-between px-3 py-2 rounded-xl text-xs font-medium transition-colors duration-150 cursor-pointer ${
                    isActive
                      ? 'bg-black/[0.06] dark:bg-white/[0.08] text-zinc-900 dark:text-zinc-100 font-semibold shadow-xs'
                      : 'text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-200 hover:bg-black/[0.02] dark:hover:bg-white/[0.03]'
                  }`}
                >
                  <div className="flex items-center gap-2.5">
                    <Icon className={`w-4 h-4 ${isActive ? 'text-[#721C24] dark:text-[#9F2D3B]' : 'text-zinc-400'}`} />
                    <span>{item.label}</span>
                  </div>

                  <span className={`text-[11px] px-1.5 py-0.5 rounded-md ${
                    isActive ? 'bg-black/[0.06] dark:bg-white/[0.1] text-zinc-900 dark:text-zinc-100' : 'text-zinc-400'
                  }`}>
                    {item.count}
                  </span>
                </button>
              );
            })}
          </nav>
        </div>
      </div>

      {/* Bottom: Minimal Storage Meter & Profile Card */}
      <div className="space-y-4 pt-4 border-t border-black/[0.05] dark:border-white/[0.06]">
        {/* Understated Minimal Storage Meter */}
        <div className="p-3 rounded-xl bg-black/[0.02] dark:bg-white/[0.02] border border-black/[0.04] dark:border-white/[0.04] space-y-2">
          <div className="flex items-center justify-between text-xs text-zinc-600 dark:text-zinc-400">
            <span className="flex items-center gap-1.5 font-medium">
              <HardDrive className="w-3.5 h-3.5" />
              <span>Storage</span>
            </span>
            <span className="text-[11px]">28%</span>
          </div>

          <div className="w-full h-1.5 bg-black/[0.06] dark:bg-white/[0.08] rounded-full overflow-hidden">
            <div
              className="h-full bg-[#721C24] dark:bg-[#9F2D3B] rounded-full"
              style={{ width: '28.4%' }}
            />
          </div>

          <p className="text-[11px] text-zinc-500 dark:text-zinc-400">
            14.2 GB of 50 GB used
          </p>
        </div>

        {/* User Profile Card */}
        <div className="flex items-center justify-between px-1">
          <div className="flex items-center gap-2.5 min-w-0">
            <div className="w-8 h-8 rounded-full bg-black/[0.06] dark:bg-white/[0.1] text-xs font-semibold text-zinc-800 dark:text-zinc-200 flex items-center justify-center shrink-0">
              {user?.avatarInitials || 'AV'}
            </div>
            <div className="min-w-0">
              <p className="text-xs font-medium text-zinc-900 dark:text-zinc-100 truncate">
                {user?.name || 'Alexander Vance'}
              </p>
              <p className="text-[10.5px] text-zinc-500 dark:text-zinc-400 truncate">
                {user?.email || 'alexander@filevault.io'}
              </p>
            </div>
          </div>

          <button
            onClick={logout}
            className="p-1.5 rounded-lg text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 hover:bg-black/[0.05] dark:hover:bg-white/[0.06] transition-colors cursor-pointer"
            title="Log out"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
