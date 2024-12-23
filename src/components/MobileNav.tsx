import { Home, Settings, List } from "lucide-react";
import { cn } from "@/lib/utils";
import { Link, useLocation } from "react-router-dom";

const MobileNav = () => {
  const location = useLocation();
  
  const tabs = [
    {
      label: "Overview",
      icon: Home,
      href: "/dashboard",
    },
    {
      label: "Patterns",
      icon: List,
      href: "/dashboard/patterns",
    },
    {
      label: "Settings",
      icon: Settings,
      href: "/dashboard/settings",
    },
  ];

  return (
    <div className="fixed bottom-0 left-0 right-0 border-t bg-background md:hidden">
      <nav className="flex justify-around">
        {tabs.map((tab) => {
          const isActive = location.pathname === tab.href;
          return (
            <Link
              key={tab.href}
              to={tab.href}
              className={cn(
                "flex flex-1 flex-col items-center justify-center py-3",
                isActive ? "text-primary" : "text-muted-foreground"
              )}
            >
              <tab.icon className="h-5 w-5" />
              <span className="text-xs">{tab.label}</span>
            </Link>
          );
        })}
      </nav>
    </div>
  );
};

export default MobileNav;