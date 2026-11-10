import { useFiles } from '../context/FileContext';
import { Sidebar } from '../components/dashboard/Sidebar';
import { Navbar } from '../components/dashboard/Navbar';
import { UploadZone } from '../components/dashboard/UploadZone';
import { FileTable } from '../components/dashboard/FileTable';
import { FileCard } from '../components/dashboard/FileCard';
import { ShareModal } from '../components/dashboard/ShareModal';
import { DeleteModal } from '../components/dashboard/DeleteModal';
import { SearchX, CheckCircle, Info, AlertCircle } from 'lucide-react';
import { Button } from '../components/common/Button';

export const DashboardPage = () => {
  const { 
    filteredFiles, 
    viewMode, 
    activeTab, 
    selectedCategory, 
    setSelectedCategory,
    searchQuery,
    setSearchQuery,
    toast 
  } = useFiles();

  const categories = [
    { id: 'all', label: 'All' },
    { id: 'documents', label: 'Documents' },
    { id: 'media', label: 'Media' },
    { id: 'archives', label: 'Archives' },
    { id: 'code', label: 'Code' }
  ];

  const getSectionTitle = () => {
    switch (activeTab) {
      case 'shared':
        return 'Shared with Me';
      case 'favorites':
        return 'Favorites';
      case 'trash':
        return 'Trash';
      default:
        return 'All Files';
    }
  };

  return (
    <div className="flex h-screen w-full bg-[#EBECEF] dark:bg-[#0F1115] text-zinc-900 dark:text-zinc-100 overflow-hidden font-sans transition-colors duration-150">
      {/* Frosted Glass Sidebar */}
      <Sidebar />

      {/* Main Workspace Area */}
      <div className="flex-1 flex flex-col min-w-0 overflow-y-auto">
        <Navbar />

        <main className="flex-1 p-6 space-y-5 max-w-7xl w-full mx-auto">
          {/* Header row: title and category filter pills */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <h2 className="text-base font-semibold tracking-tight text-zinc-900 dark:text-zinc-100">
                {getSectionTitle()}
              </h2>
              <p className="text-xs text-zinc-500 dark:text-zinc-400">
                {filteredFiles.length} file{filteredFiles.length !== 1 ? 's' : ''} found
              </p>
            </div>

            {/* Category Filter Pills (Light Glass Tags) */}
            <div className="flex items-center gap-1.5 overflow-x-auto pb-1 sm:pb-0">
              {categories.map((cat) => {
                const isSelected = selectedCategory === cat.id;
                return (
                  <button
                    key={cat.id}
                    onClick={() => setSelectedCategory(cat.id)}
                    className={`px-3 py-1.5 rounded-xl text-xs font-medium transition-colors duration-150 cursor-pointer ${
                      isSelected
                        ? 'bg-[#721C24] dark:bg-[#9F2D3B] text-white shadow-xs'
                        : 'bg-white/60 dark:bg-white/[0.04] text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100 hover:bg-white/80 dark:hover:bg-white/[0.08] border border-white/60 dark:border-white/[0.06]'
                    }`}
                  >
                    {cat.label}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Upload Zone */}
          {activeTab !== 'trash' && <UploadZone />}

          {/* Files List / Grid View */}
          {filteredFiles.length > 0 ? (
            viewMode === 'table' ? (
              <FileTable files={filteredFiles} />
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                {filteredFiles.map((file) => (
                  <FileCard key={file.id} file={file} />
                ))}
              </div>
            )
          ) : (
            <div className="flex flex-col items-center justify-center p-12 text-center rounded-2xl bg-white/40 dark:bg-white/[0.02] border border-black/[0.05] dark:border-white/[0.05] space-y-3">
              <div className="w-10 h-10 rounded-xl bg-black/[0.04] dark:bg-white/[0.06] flex items-center justify-center text-zinc-400">
                <SearchX className="w-5 h-5" />
              </div>
              <div className="space-y-1">
                <h3 className="text-xs font-semibold text-zinc-800 dark:text-zinc-200">
                  No files found
                </h3>
                <p className="text-xs text-zinc-500 max-w-xs">
                  {searchQuery
                    ? `No files match "${searchQuery}". Try a different keyword.`
                    : 'This folder is currently empty.'}
                </p>
              </div>
              {(searchQuery || selectedCategory !== 'all') && (
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => {
                    setSearchQuery('');
                    setSelectedCategory('all');
                  }}
                >
                  Reset filters
                </Button>
              )}
            </div>
          )}
        </main>
      </div>

      {/* Frosted Dialogs */}
      <ShareModal />
      <DeleteModal />

      {/* Minimal Toast Feedback */}
      {toast && (
        <div className="fixed bottom-5 right-5 z-50 flex items-center gap-2 px-4 py-2.5 rounded-xl bg-white/90 dark:bg-[#181B20]/90 backdrop-blur-md border border-white/60 dark:border-white/[0.08] text-xs text-zinc-800 dark:text-zinc-200 shadow-xl animate-in fade-in duration-150">
          {toast.type === 'success' && <CheckCircle className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400 shrink-0" />}
          {toast.type === 'warning' && <AlertCircle className="w-3.5 h-3.5 text-amber-600 dark:text-amber-400 shrink-0" />}
          {toast.type === 'info' && <Info className="w-3.5 h-3.5 text-zinc-500 shrink-0" />}
          <span>{toast.message}</span>
        </div>
      )}
    </div>
  );
};

export default DashboardPage;
