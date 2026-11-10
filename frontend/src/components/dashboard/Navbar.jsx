import { useRef } from 'react';
import { useFiles } from '../../context/FileContext';
import { Button } from '../common/Button';
import { ThemeToggle } from '../common/ThemeToggle';
import { Search, Plus, LayoutGrid, List, X } from 'lucide-react';

export const Navbar = () => {
  const { 
    searchQuery, 
    setSearchQuery, 
    viewMode, 
    setViewMode, 
    addFiles 
  } = useFiles();

  const fileInputRef = useRef(null);

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      addFiles(e.target.files);
      e.target.value = '';
    }
  };

  return (
    <header className="sticky top-0 z-20 w-full bg-white/70 dark:bg-[#181B20]/70 backdrop-blur-md border-b border-white/60 dark:border-white/[0.08] shadow-[0_4px_20px_rgba(0,0,0,0.02)] px-6 py-3 transition-colors duration-150">
      <input
        ref={fileInputRef}
        type="file"
        multiple
        onChange={handleFileChange}
        className="hidden"
      />

      <div className="flex items-center justify-between gap-4">
        {/* Search Bar */}
        <div className="relative flex-1 max-w-sm">
          <div className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400 dark:text-zinc-500 pointer-events-none">
            <Search className="w-4 h-4" />
          </div>

          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search files..."
            className="w-full h-8.5 rounded-xl bg-white/50 focus:bg-white/80 dark:bg-white/[0.04] dark:focus:bg-[#181B20] border border-black/10 dark:border-white/[0.08] pl-9 pr-8 text-xs text-zinc-900 dark:text-zinc-100 placeholder:text-zinc-400 dark:placeholder:text-zinc-500 focus:outline-none focus:ring-1 focus:ring-[#721C24]/30 dark:focus:ring-[#9F2D3B]/40 transition-colors"
          />

          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className="absolute right-2.5 top-1/2 -translate-y-1/2 text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        {/* Right side controls */}
        <div className="flex items-center gap-2.5">
          {/* View Switcher */}
          <div className="flex items-center bg-black/[0.04] dark:bg-white/[0.04] p-0.5 rounded-xl border border-black/[0.04] dark:border-white/[0.04]">
            <button
              onClick={() => setViewMode('table')}
              className={`p-1.5 rounded-lg transition-colors cursor-pointer ${
                viewMode === 'table'
                  ? 'bg-white dark:bg-zinc-800 text-zinc-900 dark:text-zinc-100 shadow-xs'
                  : 'text-zinc-500 hover:text-zinc-800 dark:hover:text-zinc-200'
              }`}
              title="Table view"
            >
              <List className="w-3.5 h-3.5" />
            </button>
            <button
              onClick={() => setViewMode('grid')}
              className={`p-1.5 rounded-lg transition-colors cursor-pointer ${
                viewMode === 'grid'
                  ? 'bg-white dark:bg-zinc-800 text-zinc-900 dark:text-zinc-100 shadow-xs'
                  : 'text-zinc-500 hover:text-zinc-800 dark:hover:text-zinc-200'
              }`}
              title="Grid view"
            >
              <LayoutGrid className="w-3.5 h-3.5" />
            </button>
          </div>

          {/* Theme Toggle (Static, No Spin) */}
          <ThemeToggle />

          {/* Primary "+ Upload File" button */}
          <Button
            variant="primary"
            size="sm"
            icon={Plus}
            onClick={() => fileInputRef.current?.click()}
          >
            Upload File
          </Button>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
