import { useFiles } from '../../context/FileContext';
import { formatBytes, formatDate } from '../../api/mockData';
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
  Users,
  Eye
} from 'lucide-react';

function getCategoryIcon(category) {
  switch (category) {
    case 'documents':
      return <FileText className="w-4 h-4 text-zinc-600 dark:text-zinc-400" />;
    case 'media':
      return <FileImage className="w-4 h-4 text-zinc-600 dark:text-zinc-400" />;
    case 'archives':
      return <FileArchive className="w-4 h-4 text-zinc-600 dark:text-zinc-400" />;
    case 'code':
      return <FileCode className="w-4 h-4 text-zinc-600 dark:text-zinc-400" />;
    default:
      return <File className="w-4 h-4 text-zinc-600 dark:text-zinc-400" />;
  }
}

export const FileTable = ({ files }) => {
  const { 
    downloadFile, 
    setShareModalFile, 
    setDeleteModalFile, 
    setPreviewModalFile,
    toggleFavorite, 
    restoreFile,
    activeTab 
  } = useFiles();

  const isTrash = activeTab === 'trash';

  return (
    <div className="w-full overflow-x-auto rounded-2xl bg-white dark:bg-[#181B20] border border-black/[0.06] dark:border-white/[0.08] shadow-xs">
      <table className="w-full text-left border-collapse text-xs">
        <thead>
          <tr className="border-b border-black/[0.05] dark:border-white/[0.06] text-zinc-400 dark:text-zinc-500 font-medium select-none">
            <th className="py-3 px-4">Name</th>
            <th className="py-3 px-3 w-28">Size</th>
            <th className="py-3 px-3 w-28">Type</th>
            <th className="py-3 px-3 w-36">Shared</th>
            <th className="py-3 px-3 w-32">Last Modified</th>
            <th className="py-3 px-4 w-16 text-right">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-black/[0.03] dark:divide-white/[0.04]">
          {files.map((file) => {
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
                    label: 'Preview',
                    icon: Eye,
                    onClick: () => setPreviewModalFile(file)
                  },
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
              <tr
                key={file.id}
                className="group hover:bg-black/[0.02] dark:hover:bg-white/[0.02] transition-colors duration-75"
              >
                {/* Name & Star */}
                <td className="py-3 px-4">
                  <div className="flex items-center gap-3 min-w-0">
                    {!isTrash && (
                      <button
                        onClick={() => toggleFavorite(file.id)}
                        className={`transition-colors cursor-pointer shrink-0 ${
                          file.isFavorite
                            ? 'text-[#721C24] dark:text-[#9F2D3B]'
                            : 'text-zinc-300 dark:text-zinc-600 hover:text-[#721C24] dark:hover:text-[#9F2D3B] opacity-0 group-hover:opacity-100'
                        }`}
                        title={file.isFavorite ? 'Remove from favorites' : 'Add to favorites'}
                      >
                        <Star className="w-3.5 h-3.5 fill-current" />
                      </button>
                    )}

                    <div 
                      onClick={() => !isTrash && setPreviewModalFile(file)}
                      className="w-7 h-7 rounded-lg bg-black/[0.03] dark:bg-white/[0.04] border border-black/[0.04] dark:border-white/[0.06] flex items-center justify-center shrink-0 cursor-pointer"
                    >
                      {getCategoryIcon(file.category)}
                    </div>

                    <span
                      onClick={() => !isTrash && setPreviewModalFile(file)}
                      className="font-medium text-zinc-800 dark:text-zinc-200 hover:text-[#721C24] dark:hover:text-[#9F2D3B] truncate cursor-pointer"
                      title={`Preview "${file.name}"`}
                    >
                      {file.name}
                    </span>
                  </div>
                </td>

                {/* Size */}
                <td className="py-3 px-3 text-zinc-500 dark:text-zinc-400 tabular-nums">
                  {formatBytes(file.size)}
                </td>

                {/* Type */}
                <td className="py-3 px-3">
                  <span className="inline-block px-2 py-0.5 rounded-md bg-black/[0.03] dark:bg-white/[0.05] border border-black/[0.04] dark:border-white/[0.04] text-[11px] text-zinc-600 dark:text-zinc-400 capitalize">
                    {file.category}
                  </span>
                </td>

                {/* Shared */}
                <td className="py-3 px-3">
                  {file.isShared ? (
                    <span 
                      onClick={() => setShareModalFile(file)}
                      className="inline-flex items-center gap-1.5 text-zinc-600 dark:text-zinc-400 text-xs cursor-pointer hover:underline"
                    >
                      <Users className="w-3.5 h-3.5 text-[#721C24] dark:text-[#9F2D3B]" />
                      <span>{file.sharedWith?.length ? `${file.sharedWith.length} shared` : 'Active'}</span>
                    </span>
                  ) : (
                    <span className="text-zinc-400 dark:text-zinc-500 text-xs">Private</span>
                  )}
                </td>

                {/* Last Modified */}
                <td className="py-3 px-3 text-zinc-500 dark:text-zinc-400 tabular-nums">
                  {formatDate(file.updatedAt)}
                </td>

                {/* Actions */}
                <td className="py-3 px-4 text-right">
                  <div className="flex items-center justify-end gap-1">
                    {!isTrash && (
                      <button
                        onClick={() => setPreviewModalFile(file)}
                        className="opacity-0 group-hover:opacity-100 p-1.5 rounded-lg text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 hover:bg-black/[0.04] dark:hover:bg-white/[0.06] transition-opacity cursor-pointer"
                        title="Preview file"
                      >
                        <Eye className="w-3.5 h-3.5" />
                      </button>
                    )}

                    <Dropdown
                      trigger={
                        <button
                          className="p-1.5 rounded-lg text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 hover:bg-black/[0.04] dark:hover:bg-white/[0.06] transition-colors"
                          title="More options"
                        >
                          <MoreVertical className="w-3.5 h-3.5" />
                        </button>
                      }
                      items={menuItems}
                    />
                  </div>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

export default FileTable;
