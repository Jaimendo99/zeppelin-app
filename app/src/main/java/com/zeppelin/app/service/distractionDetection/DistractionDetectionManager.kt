package com.zeppelin.app.service.distractionDetection

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import com.zeppelin.app.service.distractionDetection.data.DistractionDetail

/**
 * Data class to hold detailed information about a distracting app's usage.
 *
 * @param packageName The package name of the app.
 * @param totalTimeInForeground The total time the app was in the foreground during the queried interval.
 * @param usageIntervalStart The beginning of the time interval for this usage stat (UsageStats.firstTimeStamp).
 * @param usageIntervalEnd The end of the time interval for this usage stat (UsageStats.lastTimeStamp).
 * @param lastTimeAppUsed The last time the app was used (UsageStats.lastTimeUsed).
 */


class DistractionDetectionManager(private val context: Context) {

    private val usageStatsManager: UsageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }
    private val packageManager: PackageManager by lazy {
        context.packageManager
    }

    /**
     * Checks if the app has been granted permission to access usage stats.
     * This permission is required to monitor app usage.
     */
    internal fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Opens the system settings screen where the user can grant usage access permission.
     * Call this if `hasUsageStatsPermission()` returns false.
     */
    fun requestUsageStatsPermission() {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("DistractionDetectionManager", "Error opening usage access settings", e)
            // Fallback or alternative action if settings screen cannot be opened
            val intent = Intent(Settings.ACTION_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent);
        }
    }

    /**
     * Calculates the total time spent in distracting apps within the given time range.
     *
     * @param startTime The start of the time interval, in milliseconds since epoch.
     * @param endTime The end of the time interval, in milliseconds since epoch.
     * @param targetAppCategories A list of app categories to consider as distractions.
     *                            Uses `ApplicationInfo.category` constants.
     *                            Example: `listOf(ApplicationInfo.CATEGORY_GAME, ApplicationInfo.CATEGORY_SOCIAL)`.
     * @param additionalPackageNames A list of specific package names to also consider as distractions,
     *                               regardless of their category (e.g., "com.instagram.android").
     * @return Total time in milliseconds spent in distracting apps. Returns 0L if permission is not granted
     *         or if no distracting apps are found.
     */
    fun getDistractionTimeMillis(
        startTime: Long,
        endTime: Long,
        targetAppCategories: List<Int> = listOf(
            ApplicationInfo.CATEGORY_GAME,
            ApplicationInfo.CATEGORY_SOCIAL
        ),
        additionalPackageNames: List<String> = emptyList()
    ): Long {
        if (!hasUsageStatsPermission()) {
            Log.w("DistractionDetectionManager", "Usage stats permission not granted. Cannot get distraction time.")
            return 0L
        }

        val usageStatsList: List<UsageStats> =
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime) ?: emptyList()

        var totalDistractionTime = 0L

        for (usageStat in usageStatsList) {
            if (usageStat.totalTimeInForeground > 0) {
                try {
                    val appInfo = packageManager.getApplicationInfo(usageStat.packageName, 0)
                    val appCategory = appInfo.category // Direct access as minSdk is 31

                    val isDistractingByCategory = targetAppCategories.contains(appCategory)
                    val isDistractingByPackageName = additionalPackageNames.any { it.equals(usageStat.packageName, ignoreCase = true) }

                    if (isDistractingByCategory || isDistractingByPackageName) {
                        totalDistractionTime += usageStat.totalTimeInForeground
                        Log.d("DistractionDetectionManager", "Distracting app: ${usageStat.packageName}, time: ${usageStat.totalTimeInForeground}ms, category: $appCategory (Matched by category: $isDistractingByCategory, by package name: $isDistractingByPackageName)")
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    // App might have been uninstalled during the queried period
                    Log.w("DistractionDetectionManager", "Package not found: ${usageStat.packageName}", e)
                } catch (e: Exception) {
                    Log.e("DistractionDetectionManager", "Error processing usage stat for ${usageStat.packageName}", e)
                }
            }
        }
        return totalDistractionTime
    }

    /**
     * Retrieves a detailed report of distracting app usage within the given time range.
     *
     * @param startTime The start of the time interval, in milliseconds since epoch.
     * @param endTime The end of the time interval, in milliseconds since epoch.
     * @param targetAppCategories A list of app categories to consider as distractions.
     * @param additionalPackageNames A list of specific package names to also consider as distractions.
     * @return A list of `DistractionDetail` objects for each distracting app. Returns an empty list
     *         if permission is not granted or no distracting apps are found.
     */
    fun getDetailedDistractionReport(
        startTime: Long,
        endTime: Long,
        targetAppCategories: List<Int> = listOf(
            ApplicationInfo.CATEGORY_GAME,
            ApplicationInfo.CATEGORY_SOCIAL
        ),
        additionalPackageNames: List<String> = emptyList()
    ): List<DistractionDetail> {
        if (!hasUsageStatsPermission()) {
            Log.w("DistractionDetectionManager", "Usage stats permission not granted. Cannot get detailed distraction report.")
            return emptyList()
        }

        val usageStatsList: List<UsageStats> =
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime) ?: emptyList()

        val distractionDetailsList = mutableListOf<DistractionDetail>()

        for (usageStat in usageStatsList) {
            if (usageStat.totalTimeInForeground > 0) {
                try {
                    val appInfo = packageManager.getApplicationInfo(usageStat.packageName, 0)
                    val appCategory = appInfo.category

                    val isDistractingByCategory = targetAppCategories.contains(appCategory)
                    val isDistractingByPackageName = additionalPackageNames.any { it.equals(usageStat.packageName, ignoreCase = true) }

                    if (isDistractingByCategory || isDistractingByPackageName) {
                        val detail = DistractionDetail(
                            packageName = usageStat.packageName,
                            totalTimeInForeground = usageStat.totalTimeInForeground,
                            usageIntervalStart = usageStat.firstTimeStamp,
                            usageIntervalEnd = usageStat.lastTimeStamp,
                            lastTimeAppUsed = usageStat.lastTimeUsed
                        )
                        distractionDetailsList.add(detail)
                        Log.d("DistractionDetectionManager", "Distracting app detail: $detail (Matched by category: $isDistractingByCategory, by package name: $isDistractingByPackageName)")
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.w("DistractionDetectionManager", "Package not found while generating report: ${usageStat.packageName}", e)
                } catch (e: Exception) {
                    Log.e("DistractionDetectionManager", "Error processing usage stat for report: ${usageStat.packageName}", e)
                }
            }
        }
        return distractionDetailsList
    }


    /**
     * Convenience method to get distraction time for the last 24 hours.
     */
    fun getDistractionTimeLast24Hours(
        targetAppCategories: List<Int> = listOf(
            ApplicationInfo.CATEGORY_GAME,
            ApplicationInfo.CATEGORY_SOCIAL
        ),
        additionalPackageNames: List<String> = emptyList()
    ): Long {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (24 * 60 * 60 * 1000) // 24 hours ago
        return getDistractionTimeMillis(startTime, endTime, targetAppCategories, additionalPackageNames)
    }
}