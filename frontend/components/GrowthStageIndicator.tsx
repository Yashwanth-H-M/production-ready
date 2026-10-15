interface Props {
  stage: string;
}

const STAGES = ['propagating', 'growing', 'mature', 'sold'];

export function GrowthStageIndicator({ stage }: Props) {
  const activeIndex = STAGES.indexOf(stage);
  const isDead = stage === 'dead';

  return (
    <div className="flex items-center gap-1.5" aria-label={`Batch lifecycle: ${stage}`}>
      <span className="flex gap-1">
        {STAGES.map((label, index) => {
          const filled = isDead ? index <= 1 : index <= activeIndex;
          return <span key={label} title={label} className={`h-1.5 w-6 rounded-full transition-colors ${filled ? (isDead ? 'bg-red-400' : 'bg-[#0F7C61]') : 'bg-slate-200'}`} />;
        })}
      </span>
      <span className={`ml-1 text-[10px] font-bold capitalize ${isDead ? 'text-red-600' : 'text-slate-500'}`}>{stage}</span>
    </div>
  );
}
