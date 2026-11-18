# Rules

Rules are independently configurable and enabled by default. Single-argument calls may be inline or multiline; the formatter preserves that choice outside standalone named-argument Compose UI calls. Compaction preserves comments and respects `max_line_length`, including code after the expression being joined. Standard KtLint rules handle indentation and trailing commas.

Rules prefixed with `compose-` apply inside `@Composable` functions and explicitly annotated composable lambdas, including annotation aliases. Ordinary helper functions keep standard formatting. UI calls are identified by an uppercase callee.

| Configuration setting | Project default |
| --- | --- |
| `indent_size` | `4` |
| `max_line_length` | `140` |

Disable an individual rule with `disabled` instead of `enabled` in its `.editorconfig` setting, or suppress it by rule ID:

```kotlin
@Suppress("ktlint:matee:comparison-wrapping")
```

## Multiline call arguments

Put each argument of a call with two or more parenthesized arguments on a separate line when at least one argument is named. Purely positional calls keep their layout unless standard KtLint rules require wrapping for length. Trailing lambdas do not count.

### KtLint

```kotlin
val value = Result(
    first = first,
    second = second,
)
```

### Disallowed

```kotlin
val value = Result(first = first, second = second)
```

Rule id: `matee:multiline-call-arguments`

```editorconfig
ktlint_matee_multiline-call-arguments = enabled
```

## Argument value wrapping

Keep a named argument and its single-line value together when they fit.

### KtLint

```kotlin
consume(
    title = stringResource(resource),
    enabled = enabled,
)
```

### Disallowed

```kotlin
consume(
    title =
        stringResource(resource),
    enabled = enabled,
)
```

Rule id: `matee:argument-value-wrapping`

```editorconfig
ktlint_matee_argument-value-wrapping = enabled
```

## Property initializer wrapping

Keep the first line of a call or binary-expression initializer beside `=` when it fits.

### KtLint

```kotlin
val values = listOf(
    first,
    second,
)
```

### Disallowed

```kotlin
val values =
    listOf(
        first,
        second,
    )
```

Rule id: `matee:property-initializer-wrapping`

```editorconfig
ktlint_matee_property-initializer-wrapping = enabled
```

## Comparison wrapping

Keep a short comparison on one line when both operands are single-line expressions.

### KtLint

```kotlin
val missing = value == null
```

### Disallowed

```kotlin
val missing = value ==
    null
```

Rule id: `matee:comparison-wrapping`

```editorconfig
ktlint_matee_comparison-wrapping = enabled
```

## If condition wrapping

Keep a short, single-line condition beside its parentheses. Comparison wrapping inside the condition is handled separately.

### KtLint

```kotlin
if (ready) {
    work()
}
```

### Disallowed

```kotlin
if (
    ready
) {
    work()
}
```

Rule id: `matee:if-condition-wrapping`

```editorconfig
ktlint_matee_if-condition-wrapping = enabled
```

## When branch wrapping

Start a multiline `if` expression used as a `when` branch below the arrow.

### KtLint

```kotlin
when (value) {
    else ->
        if (ready) {
            first()
        } else {
            second()
        }
}
```

### Disallowed

```kotlin
when (value) {
    else -> if (ready) {
        first()
    } else {
        second()
    }
}
```

Rule id: `matee:when-branch-wrapping`

```editorconfig
ktlint_matee_when-branch-wrapping = enabled
```

## Compose single-argument call

Wrap a standalone UI call with one named argument in a multiline Compose block. Positional calls such as `LaunchedEffect(key)` keep their layout. Tiny inline slots such as `label = { Text(text = title) }` may stay compact.

### KtLint

```kotlin
Column {
    Text(
        text = title,
    )
}
```

### Disallowed

```kotlin
Column {
    Text(text = title)
}
```

Rule id: `matee:compose-single-argument-call`

```editorconfig
ktlint_matee_compose-single-argument-call = enabled
```

## Compose chain wrapping

Put each call in a non-modifier chain below its receiver when the chain contains at least two calls. Property references and single calls such as `draft.copy(...)` stay together.

### KtLint

```kotlin
val names = values
    .filter { it.enabled }
    .map { it.name }
```

### Disallowed

```kotlin
val names = values.filter { it.enabled }.map { it.name }
```

Rule id: `matee:compose-chain-wrapping`

```editorconfig
ktlint_matee_compose-chain-wrapping = enabled
```

## Compose modifier chain wrapping

Put every call on `Modifier` or `modifier` below its receiver, including a single call. This rule owns modifier chains; Compose chain wrapping handles other chains.

### KtLint

```kotlin
modifier = Modifier
    .fillMaxWidth()
```

### Disallowed

```kotlin
modifier = Modifier.fillMaxWidth()
```

Rule id: `matee:compose-modifier-chain-wrapping`

```editorconfig
ktlint_matee_compose-modifier-chain-wrapping = enabled
```

## Compose UI spacing

Separate adjacent multiline UI calls and state/UI pairs with a blank line. Comments between statements retain their grouping.

### KtLint

```kotlin
Text(
    text = title,
)

Text(
    text = subtitle,
)
```

### Disallowed

```kotlin
Text(
    text = title,
)
Text(
    text = subtitle,
)
```

Rule id: `matee:compose-ui-spacing`

```editorconfig
ktlint_matee_compose-ui-spacing = enabled
```

## Compose modifier argument order

Put the named `modifier` argument last inside the parentheses, before any trailing lambda. Reports a violation without an automatic fix.

### KtLint

```kotlin
Text(
    text = title,
    modifier = modifier,
)
```

### Disallowed

```kotlin
Text(
    modifier = modifier,
    text = title,
)
```

Rule id: `matee:compose-modifier-argument-order`

```editorconfig
ktlint_matee_compose-modifier-argument-order = enabled
```
