package cz.matee.ruleset

import com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.pinterest.ktlint.rule.engine.core.api.RuleSetId

class MateeRuleSetProvider : RuleSetProviderV3(RuleSetId("matee")) {
    override fun getRuleProviders(): Set<RuleProvider> =
        setOf(
            RuleProvider { CallLayoutRule() },
            RuleProvider { ComposeLayoutRule() },
        )
}
