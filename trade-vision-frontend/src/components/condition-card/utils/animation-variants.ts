export const cardVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: { opacity: 1, y: 0, transition: { duration: 0.3 } },
    exit: { opacity: 0, scale: 0.95, transition: { duration: 0.2 } }
  };
  
  // Animation variants for parameter fields
  export const paramVariants = {
    hidden: { opacity: 0, x: -10 },
    visible: (i: number) => ({ 
      opacity: 1, 
      x: 0, 
      transition: { delay: i * 0.05, duration: 0.2 } 
    })
  };
  
  // Animation variants for nested condition containers
  export const nestedContainerVariants = {
    hidden: { opacity: 0, height: 0 },
    visible: { opacity: 1, height: 'auto', transition: { duration: 0.3 } }
  };
  
  // Animation variants for glowing indicators
  export const glowAnimation = (color: string) => {
    return {
      boxShadow: [
        `0 0 5px ${color}`,
        `0 0 10px ${color}`,
        `0 0 5px ${color}`
      ],
      transition: {
        duration: 2,
        repeat: Infinity,
        repeatType: "reverse" as const 
      }
    };
  };
  
  // Animation variants for bottom pill indicator
  export const pillAnimation = {
    opacity: [0.5, 0.8, 0.5],
    transition: { duration: 2, repeat: Infinity }
  };