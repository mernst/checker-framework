#! /bin/sh

# Install the NJR set of Java programs.

# As of December 2024, NJR is abandonware.  I reported bugs to Jens Palsberg, and
# he agreed they should be fixed but said he didn't have a student to do the work.
# (And I guess he wasn't willing to do it himself.)

export NJR=/tmp/njr
mkdir -p $NJR
cd $NJR || exit 2
wget -Nq https://zenodo.org/records/4632231/files/benchmark_stats.csv
wget -Nq https://zenodo.org/records/4632231/files/njr-1_dataset.zip
wget -Nq https://zenodo.org/records/4632231/files/scripts.zip
unzip -q scripts.zip
unzip -q njr-1_dataset.zip
ln -s njr-1_dataset final_dataset

# Remove projects that lead to an error like "BadClassFile" or "class file for ... not found"
cd $NJR/final_dataset || exit 2
rm -rf \
  url433bec1e56_sarobe_DifficultyInvestigation_tgz-pJ8-fr_inria_optimization_cmaes_examples_CMAExample1J8 \
  url708d477f92_zSakare_SENG3011_tgz-pJ8-test_runner_TestRunnerJ8 \
  url80e7c3d65d_sblit_dcl_tgz-pJ8-org_dclayer_DCLJ8 \
  url8be3f02398_vnamboo_LearnJava_tgz-pJ8-org_json_TestJ8 \
  urlc58f2e0b85_xoriole_CEAC_tgz-pJ8-com_sdm_phr_cpabe_CpabeTestJ8
# Remove projects that lead to "package ... does not exist" or "cannot find symbol" or "package ... is not visible" or "method does not override or implement a method from a supertype" on Java >8; or that use Swing
cd $NJR/final_dataset || exit 2
rm -rf \
  url1cbaffb197_foxking0416_AlgorithmTraining_tgz-pJ8-algorithm_leetcode_MainLeetJ8 \
  url2f44efcfa6_wangyudong_sohutw_java_sdk_2_0_tgz-pJ8-com_sohu_t_api_sdk_json_TestJ8 \
  url47af6b1535_rolandotr_db_comparison_tgz-pJ8-methodology_HistoryJ8 \
  url47ebb27197_asirbu_CorrectiveEvolution_tgz-pJ8-eu_fbk_soa_evolution_engine_impl_test_ProblemToSMVFuncTestJ8 \
  url4c41254592_topskychen_AuthTopk_tgz-pJ8-quickhull3d_QuickHull3DTestJ8 \
  url64b10c0106_schlabberdog_blocks_tgz-pJ8-com_github_users_schlabberdog_blocks_ui_CliUIJ8 \
  url6f34cc586e_mekku93_sxl_interpreter_repl_tgz-pJ8-com_miguel_sxl_SXLJ8 \
  url78eec0e0b6_Orikuro_vic_tgz-pJ8-net_nexon_vindictus_itemcomparer_inport_StaticImportJ8 \
  url9e7e48285f_nalbachk_MidiToPhaseShift_tgz-pJ8-com_my_main_MainJ8 \
  url61db808cfe_ctriposs_bigcache_tgz-pJ8-com_ctriposs_bigcache_BigCacheStressTestJ8 \
  url27a7ae3508_serkan_ozal_ocean_of_memories_tgz-pJ8-com_zeroturnaround_rebellabs_oceanofmemories_article1_objectlayout_ObjectMemoryLayoutDemoJ8 \
  url97f33aa931_wnr_Othello_tgz-pJ8-kth_game_othello_tournament_MainJ8 \
  url58e9ba4d92_novokrest_DrunkardGame_tgz-pJ8-DrunkardGame_MainJ8 \
  url73e3057d61_empeeoh_BACnet4J_tgz-pJ8-com_serotonin_bacnet4j_test_OverflowTestJ8 \
  url8af84d296f_mrcosta_gantt_generator_tgz-pJ8-org_gantt_generator_mrcpsp_RunJ8 \
  url2a908001cd_malictus_klang_tgz-pJ8-malictus_klang_test_PrimitiveTestJ8
# Temporary: Remove projects for which Resource Leak inference runs for hours.
cd $NJR/final_dataset || exit 2
rm -rf \
  url1a41b8e9f2_mr1azl_INF345_tgz-pJ8-TestParserJ8 \
  url3f8d6440dd_huiwq1990_CognativeModel_tgz-pJ8-clarion_samples_HelloWorldJ8 \
  url1a41b8e9f2_mr1azl_INF345_tgz-pJ8-TestParserJ8 \
  url647b55da7c_Nikhilkumar13_emuSr_tgz-pJ8-MainJ8 \
  url8db16c7d62_Arsaell_EdT_tgz-pJ8-DATA_MainJ8 \
  url8e6440c8aa_robdimsdale_project_euler_tgz-pJ8-com_rmd_personal_projecteuler_MainJ8

cd $NJR || exit 2
