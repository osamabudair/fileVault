import { useFiles } from '../../context/FileContext';
import { formatBytes, formatDate } from '../../api/mockData';
import { GlassCard } from '../common/GlassCard';
import { Dropdown } from '../common/Dropdown';
import { 
  FileText, 
  FileArchive, 
  FileImage, 
  FileCode, 
  File, 
  MoreVertical, 
  Download, 
  Share2, 
  Trash2, 
  Star, 
  RotateCcw,
  Users
} from 'lucide-react';

function getCategoryIcon(category) {
  switch (category) {
    case 'documents':
      return <FileText className="w-5 h-5 text-zinc-600 dark:text-zinc-400" />;
    case 'media':
      return <FileImage className="w-5 h-5 text-zinc-600 dark:text-zinc-400" />;
    case 'archives':
      return <FileArchive className="w-5 h-5 text-zinc-600 dark:text-zinc-400" />;
    case 'code':
      return <FileCode className="w-5 h-5 text-zinc-600 dark:text-zinc-400" />;
    default:
      return <File className="w-5 h-5 text-zinc-600 dark:text-zinc-400" />;
  }
}

export const FileCard = ({ file }) => {
  const { 
    downloadFile, 
    setShareModalFile, 
    setDeleteModalFile, 
    toggleFavorite, 
    restoreFile,
    activeTab 
  } = useFiles();

  const isTrash = activeTab === 'trash';

  const menuItems = isTrash
    ? [
        {
          label: 'Restore File',
          icon: RotateCcw,
          onClick: () => restoreFile(file.id)
        },
        {
          label: 'Delete Permanently',
          icon: Trash2,
          danger: true,
          onClick: () => setDeleteModalFile(file)
        }
      ]
    : [
        {
          label: 'Download',
          icon: Download,
          onClick: () => downloadFile(file)
        },
        {
          label: 'Share Link',
          icon: Share2,
          onClick: () => setShareModalFile(file)
        },
        {
          label: file.isFavorite ? 'Remove Favorite' : 'Mark as Favorite',
          icon: Star,
          onClick: () => toggleFavorite(file.id)
        },
        { divider: true },
        {
          label: 'Move to Trash',
          icon: Trash2,
          danger: true,
          onClick: () => setDeleteModalFile(file)
        }
      ];

  return (
    <GlassCard className="p-4 flex flex-col justify-between group hoverEffect">
      {/* Top row */}
      <div className="flex items-start justify-between">
        <div className="w-9 h-9 rounded-xl bg-black/[0.03] dark:bg-white/[0.04] border border-black/[0.04] dark:border-white/[0.06] flex items-center justify-center">
          {getCategoryIcon(file.category)}
        </div>

        <div className="flex items-center gap-1">
          {!isTrash && (
            <button
              onClick={() => toggleFavorite(file.id)}
              className={`p-1 rounded-lg text-zinc-300 dark:text-zinc-600 hover:text-amber-500 dark:hover:text-amber-400 transition-colors cursor-pointer ${
                file.isFavorite ? 'text-amber-500 dark:text-amber-400' : 'opacity-0 group-hover:opacity-100'
              }`}
            >
              <Star className="w-3.5 h-3.5 fill-current" />
            </button>
          )}

          <Dropdown
            trigger={
              <button className="p-1 rounded-lg text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 hover:bg-black/[0.04] dark:hover:bg-white/[0.06] transition-colors">
                <MoreVertical className="w-3.5 h-3.5" />
              </button>
            }
            items={menuItems}
          />
        </div>
      </div>

      {/* Middle row: File name */}
      <div className="my-3">
        <h4
          onClick={() => !isTrash && downloadFile(file)}
          className="text-xs font-medium text-zinc-900 dark:text-zinc-100 hover:text-[#721C24] dark:hover:text-[#9F2D3B] truncate cursor-pointer"
          title={file.name}
        >
          {file.name}
        </h4>
        <p className="text-[11px] text-zinc-400 dark:text-zinc-500 capitalize mt-0.5">
          {file.category}
        </p>
      </div>

      {/* Bottom row: Size, Shared, Date */}
      <div className="pt-2.5 border-t border-black/[0.04] dark:border-white/[0.04] flex items-center justify-between text-[11px] text-zinc-500 dark:text-zinc-400">
        <div className="flex items-center gap-2">
          <span className="tabular-nums">{formatBytes(file.size)}</span>
          {file.isShared && (
            <span
              onClick={() => setShareModalFile(file)}
              className="inline-flex items-center gap-1 text-[#721C24] dark:text-[#9F2D3B] cursor-pointer"
              title="Shared"
            >
              <Users className="w-3 h-3" />
            </span>
          )}
        </div>
        <span>{formatDate(file.updatedAt)}</span>
      </div>
    </GlassCard>
  );
};

export default FileCard;
