import { Gift } from "lucide-react";
import { cn } from "@/lib/utils";

/**
 * Imagem padrão de presente — usada quando o item não tem foto. Gradiente rosé→lilás
 * com um laço de presente, no clima da marca. O tamanho do ícone acompanha o container.
 */
export function GiftPlaceholder({ className }: { className?: string }) {
  return (
    <div
      className={cn(
        "relative grid place-items-center overflow-hidden bg-[linear-gradient(135deg,#F7D9E7_0%,#F5F0FB_55%,#E7D9F2_100%)]",
        className,
      )}
      aria-hidden="true"
    >
      {/* brilhos sutis */}
      <span className="absolute left-[14%] top-[18%] size-1.5 rounded-full bg-white/70" />
      <span className="absolute right-[16%] top-[26%] size-1 rounded-full bg-white/60" />
      <span className="absolute bottom-[18%] left-[24%] size-1 rounded-full bg-white/50" />
      <Gift className="h-2/5 w-2/5 text-plum/45" strokeWidth={1.4} />
    </div>
  );
}
